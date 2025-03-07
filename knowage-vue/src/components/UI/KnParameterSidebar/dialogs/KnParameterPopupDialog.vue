<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary" :contentStyle="knParameterPopupDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #start> {{ $t('common.parameter') + ': ' }} {{ popupData ? popupData.idParam : '' }} </template>
            </Toolbar>
        </template>
        <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />

        <KnParameterPopupTable v-if="parameterPopUpData && !loading" :parameterPopUpData="popupData" :multivalue="multivalue" :multipleSelectedRows="multipleSelectedRows" @selected="onRowSelected"></KnParameterPopupTable>

        <template #footer>
            <div class="p-d-flex p-flex-row p-jc-end">
                <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
                <Button class="kn-button kn-button--primary" @click="save"> {{ $t('common.save') }}</Button>
            </div>
        </template>
    </Dialog>
</template>

<script lang="ts">
    import { defineComponent } from 'vue'
    import { iAdmissibleValues, iParameter } from '../KnParameterSidebar'
    import Dialog from 'primevue/dialog'
    import knParameterPopupDialogDescriptor from './KnParameterPopupDialogDescriptor.json'
    import KnParameterPopupTable from './KnParameterPopupTable.vue'

    export default defineComponent({
        name: 'kn-parameter-popup-dialog',
        components: { Dialog, KnParameterPopupTable },
        props: { visible: { type: Boolean }, selectedParameter: { type: Object }, propLoading: { type: Boolean }, parameterPopUpData: { type: Object } },
        emits: ['close', 'save'],
        data() {
            return {
                knParameterPopupDialogDescriptor,
                parameter: null as iParameter | null,
                popupData: null as iAdmissibleValues | null,
                selectedRow: null as any,
                multipleSelectedRows: [] as any[],
                multivalue: false,
                loading: false
            }
        },
        watch: {
            visible() {
                this.loadParamaterData()
            },
            selectedParameter() {
                this.loadParamaterData()
            },
            propLoading() {
                this.setLoading()
            },
            parameterPopUpData() {
                this.loadPopupData()
                this.loadParamaterData()
            }
        },
        created() {
            this.setLoading()
            this.loadParamaterData()
        },
        methods: {
            loadParamaterData() {
                this.loading = true
                this.loadParameter()
                this.loadPopupData()
                if (this.multivalue) {
                    this.setMultipleSelectedRows()
                } else {
                    this.setSelectedRow()
                }
                this.loading = false
            },
            loadParameter() {
                this.parameter = this.selectedParameter as iParameter
                this.multivalue = this.selectedParameter?.multivalue
            },
            setMultipleSelectedRows() {
                this.multipleSelectedRows = []
                const valueColumn = this.popupData?.result.metadata.valueColumn
                const descriptionColumn = this.popupData?.result.metadata.descriptionColumn

                const valueIndex = Object.keys(this.parameter?.metadata.colsMap).find((key: string) => this.parameter?.metadata.colsMap[key] === valueColumn)
                const descriptionIndex = Object.keys(this.parameter?.metadata.colsMap).find((key: string) => this.parameter?.metadata.colsMap[key] === descriptionColumn)

                const parameterValues = this.parameter?.parameterValue as any

                for (let i = 0; i < parameterValues.length; i++) {
                    const tempParameter = parameterValues[i]

                    if (valueIndex && descriptionIndex) {
                        const selectedIndex = this.parameterPopUpData?.result.data.findIndex((el: any) => {
                            return el[valueIndex] === tempParameter.value && el[descriptionIndex] === tempParameter.description
                        }) as any
                        if (selectedIndex !== -1) {
                            this.multipleSelectedRows.push(this.parameterPopUpData?.result.data[selectedIndex])
                        }
                    }
                }
            },
            setSelectedRow() {
                const valueColumn = this.popupData?.result.metadata.valueColumn
                const descriptionColumn = this.popupData?.result.metadata.descriptionColumn

                if (this.parameter?.metadata.colsMap) {
                    const valueIndex = Object.keys(this.parameter?.metadata.colsMap).find((key: string) => this.parameter?.metadata.colsMap[key] === valueColumn)
                    const descriptionIndex = Object.keys(this.parameter?.metadata.colsMap).find((key: string) => this.parameter?.metadata.colsMap[key] === descriptionColumn)

                    if (valueIndex && descriptionIndex) {
                        const selectedIndex = this.parameterPopUpData?.result.data.findIndex((el: any) => {
                            return el[valueIndex] === this.parameter?.parameterValue[0].value && el[descriptionIndex] === this.parameter?.parameterValue[0].description
                        }) as any
                        if (selectedIndex !== -1 && this.popupData) {
                            this.multipleSelectedRows = [this.popupData?.result.data[selectedIndex]]
                            this.popupData.result.data = this.popupData?.result.data.filter((el: any) => {
                                return el[valueIndex] !== this.multipleSelectedRows[0][valueIndex] || el[descriptionIndex] !== this.multipleSelectedRows[0][descriptionIndex]
                            })
                            this.popupData?.result.data.unshift(this.multipleSelectedRows[0])
                            this.selectedRow = this.multipleSelectedRows[0]
                        }
                    }
                }
            },
            loadPopupData() {
                this.popupData = this.parameterPopUpData as iAdmissibleValues
            },

            setLoading() {
                this.loading = this.propLoading
            },
            closeDialog() {
                this.$emit('close')
                this.loadParameter()
                this.popupData = null
                this.selectedRow = null
            },
            onRowSelected(selectedRows: any) {
                if (!this.multivalue) {
                    this.selectedRow = selectedRows
                } else {
                    this.multipleSelectedRows = selectedRows
                }
            },
            save() {
                if (!this.parameter) return

                const valueColumn = this.popupData?.result.metadata.valueColumn
                const descriptionColumn = this.popupData?.result.metadata.descriptionColumn

                const valueIndex = Object.keys(this.parameter.metadata.colsMap).find((key: string) => this.parameter?.metadata.colsMap[key] === valueColumn)
                const descriptionIndex = Object.keys(this.parameter.metadata.colsMap).find((key: string) => this.parameter?.metadata.colsMap[key] === descriptionColumn)

                if (!this.multivalue) {
                    this.parameter.parameterValue = this.selectedRow ? [{ value: valueIndex ? this.selectedRow[valueIndex] : '', description: descriptionIndex ? this.selectedRow[descriptionIndex] : '' }] : []

                    this.selectedRow = null
                } else {
                    this.parameter.parameterValue = []
                    this.multipleSelectedRows?.forEach((el: any) => this.parameter?.parameterValue.push({ value: valueIndex ? el[valueIndex] : '', description: descriptionIndex ? el[descriptionIndex] : '' }))
                }

                this.popupData = null
                this.$emit('save', this.parameter)
            }
        }
    })
</script>
