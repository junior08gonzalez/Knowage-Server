<template>
    <div class="p-grid p-m-0 kn-flex">
        <div class="p-col-7 p-m-0 p-p-0 right-border p-d-flex p-flex-column kn-flex">
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    {{ $t('documentExecution.documentDetails.info.infoTitle') }}
                </template>
                <template #end>
                    <Button :label="$t('documentExecution.olap.openDesigner')" class="p-button-text p-button-plain" @click="openDesignerConfirm" />
                </template>
            </Toolbar>
            <div id="informations-content" class="kn-flex kn-relative">
                <div :style="mainDescriptor.style.absoluteScroll">
                    <Card class="p-m-2">
                        <template #content>
                            <div id="upload-template-container" v-if="templates.length == 0">
                                <div class="p-field p-col-12 p-d-flex">
                                    <div class="kn-flex">
                                        <span class="p-float-label">
                                            <InputText id="fileName" class="kn-material-input" v-model="templateToUpload.name" :disabled="true" />
                                            <label for="fileName" class="kn-material-input-label"> {{ $t('documentExecution.documentDetails.info.uploadTemplate') }} </label>
                                        </span>
                                    </div>
                                    <Button icon="fas fa-upload fa-1x" class="p-button-text p-button-plain p-ml-2" @click="setUploadType" />
                                    <KnInputFile label="" v-if="!uploading" :changeFunction="setTemplateForUpload" :triggerInput="triggerUpload" />
                                </div>
                            </div>
                            <form class="p-fluid p-formgrid p-grid p-m-1">
                                <div class="p-field p-col-12 p-lg-6">
                                    <span class="p-float-label">
                                        <InputText
                                            id="label"
                                            class="kn-material-input"
                                            type="text"
                                            maxLength="100"
                                            v-model="v$.document.label.$model"
                                            :class="{
                                                'p-invalid': v$.document.label.$invalid && v$.document.label.$dirty
                                            }"
                                            @blur="v$.document.label.$touch()"
                                            @change="$emit('touched')"
                                        />
                                        <label for="label" class="kn-material-input-label"> {{ $t('common.label') }} * </label>
                                    </span>
                                    <KnValidationMessages class="p-mt-1" :vComp="v$.document.label" :additionalTranslateParams="{ fieldName: $t('common.label') }" />
                                </div>
                                <div class="p-field p-col-12 p-lg-6">
                                    <span class="p-float-label">
                                        <InputText
                                            id="name"
                                            class="kn-material-input"
                                            type="text"
                                            maxLength="200"
                                            v-model="v$.document.name.$model"
                                            :class="{
                                                'p-invalid': v$.document.name.$invalid && v$.document.name.$dirty
                                            }"
                                            @blur="v$.document.name.$touch()"
                                            @change="$emit('touched')"
                                        />
                                        <label for="name" class="kn-material-input-label"> {{ $t('common.name') }} * </label>
                                    </span>
                                    <KnValidationMessages class="p-mt-1" :vComp="v$.document.name" :additionalTranslateParams="{ fieldName: $t('common.name') }" />
                                </div>

                                <div class="p-field p-col-12 p-lg-6">
                                    <img id="image-preview" :src="getImageUrl" :height="mainDescriptor.style.previewImage" />
                                </div>

                                <div class="p-field p-col-12 p-lg-6">
                                    <span class="p-float-label">
                                        <Textarea
                                            id="description"
                                            class="kn-material-input"
                                            rows="9"
                                            maxLength="400"
                                            :autoResize="true"
                                            v-model="v$.document.description.$model"
                                            :class="{
                                                'p-invalid': v$.document.description.$invalid && v$.document.description.$dirty
                                            }"
                                            @blur="v$.document.description.$touch()"
                                            @change="$emit('touched')"
                                        />
                                        <label for="description" class="kn-material-input-label"> {{ $t('common.description') }} </label>
                                    </span>
                                    <KnValidationMessages class="p-mt-1" :vComp="v$.document.description" :additionalTranslateParams="{ fieldName: $t('common.description') }" />
                                </div>

                                <div class="p-field p-col-12 p-d-flex">
                                    <div class="kn-flex">
                                        <span class="p-float-label">
                                            <InputText id="fileName" class="kn-material-input" v-model="document.previewFile" :disabled="true" />
                                            <label for="fileName" class="kn-material-input-label"> {{ $t('documentExecution.documentDetails.info.previewImage') }} </label>
                                        </span>
                                    </div>
                                    <Button icon="fas fa-upload fa-1x" class="p-button-text p-button-plain p-ml-2" @click="setImageUploadType" />
                                    <Button v-if="document.previewFile" icon="fas fa-trash fa-1x" class="p-button-text p-button-plain p-ml-2" @click="$emit('deleteImage')" />
                                    <KnInputFile :changeFunction="setImageForUpload" accept=".png, .jpg, .jpeg" :triggerInput="triggerImageUpload" />
                                </div>
                                <div class="p-field p-col-12 p-lg-6">
                                    <span class="p-float-label">
                                        <Dropdown
                                            id="type"
                                            class="kn-material-input"
                                            v-model="v$.document.typeCode.$model"
                                            :options="documentTypes"
                                            optionLabel="translatedValueName"
                                            optionValue="valueCd"
                                            :class="{
                                                'p-invalid': v$.document.typeCode.$invalid && v$.document.typeCode.$dirty
                                            }"
                                            @blur="v$.document.typeCode.$touch()"
                                            @change="onTypeChange"
                                        />
                                        <label for="type" class="kn-material-input-label"> {{ $t('importExport.catalogFunction.column.type') }} *</label>
                                    </span>
                                    <KnValidationMessages class="p-mt-1" :vComp="v$.document.typeCode" :additionalTranslateParams="{ fieldName: $t('importExport.catalogFunction.column.type') }" />
                                </div>
                                <div class="p-field p-col-12 p-lg-6">
                                    <span class="p-float-label">
                                        <Dropdown
                                            id="engine"
                                            class="kn-material-input"
                                            v-model="v$.document.engine.$model"
                                            :options="filteredEngines"
                                            optionLabel="name"
                                            optionValue="label"
                                            :disabled="!document.typeCode || document.typeCode == ''"
                                            :class="{
                                                'p-invalid': v$.document.engine.$invalid && v$.document.engine.$dirty
                                            }"
                                            @blur="v$.document.engine.$touch()"
                                            @change="$emit('touched')"
                                        />
                                        <label for="engine" class="kn-material-input-label"> {{ $t('documentExecution.documentDetails.info.engine') }} *</label>
                                    </span>
                                    <small>{{ $t('documentExecution.documentDetails.info.engineHint') }}</small>
                                    <KnValidationMessages class="p-mt-1" :vComp="v$.document.engine" :additionalTranslateParams="{ fieldName: $t('documentExecution.documentDetails.info.engine') }" />
                                </div>

                                <span v-if="isDataSourceVisible" class="p-field p-float-label p-col-12 p-lg-6" v-bind:class="{ 'p-lg-12': !isDataSetVisible }">
                                    <Dropdown id="datasource" class="kn-material-input" v-model="document.dataSourceLabel" :options="availableDatasources" optionLabel="label" optionValue="label" />
                                    <label for="datasource" class="kn-material-input-label"> {{ $t('managers.businessModelManager.dataSource') }} </label>
                                </span>

                                <div v-if="isDataSetVisible" class="p-field p-col-12 p-lg-6 p-d-flex">
                                    <div class="kn-flex">
                                        <span class="p-float-label">
                                            <InputText id="dataset" class="kn-material-input" v-model="dataset.name" :disabled="true" />
                                            <label for="dataset" class="kn-material-input-label"> {{ $t('common.dataset') }} </label>
                                        </span>
                                    </div>
                                    <Button v-if="isDataSetVisible" icon="fas fa-search fa-1x" class="p-button-text p-button-plain p-ml-2" @click="showDatasetDialog = true" />
                                </div>

                                <div class="p-field p-col-12 p-lg-6">
                                    <span class="p-float-label">
                                        <Dropdown
                                            id="state"
                                            class="kn-material-input"
                                            v-model="v$.document.stateCode.$model"
                                            :options="availableStates"
                                            optionLabel="translatedValueName"
                                            optionValue="valueCd"
                                            :class="{
                                                'p-invalid': v$.document.stateCode.$invalid && v$.document.stateCode.$dirty
                                            }"
                                            @blur="v$.document.stateCode.$touch()"
                                            @change="$emit('touched')"
                                        />
                                        <label for="state" class="kn-material-input-label"> {{ $t('common.state') }} *</label>
                                    </span>
                                    <KnValidationMessages class="p-mt-1" :vComp="v$.document.stateCode" :additionalTranslateParams="{ fieldName: $t('common.state') }" />
                                </div>
                                <div class="p-field p-col-12 p-lg-6">
                                    <span class="p-float-label">
                                        <InputText id="refresh" class="kn-material-input" v-model="document.refreshSeconds" type="number" />
                                        <label for="refresh" class="kn-material-input-label"> {{ $t('documentExecution.documentDetails.info.refresh') }} </label>
                                    </span>
                                    <small>{{ $t('documentExecution.documentDetails.info.refreshHint') }}</small>
                                </div>
                                <span class="p-field p-col-12 p-lg-6 p-jc-center p-mt-3">
                                    <InputSwitch id="visible" v-model="document.visible" />
                                    <i class="far fa-eye p-ml-2" />
                                    <label for="visible" class="kn-material-input-label p-ml-2"> {{ $t('common.visible') }} </label>
                                </span>
                                <span class="p-field p-col-12 p-lg-6 p-mt-3">
                                    <InputSwitch id="locked" v-model="lockedByUser" @change="setIsLockedByUser" />
                                    <i class="fas fa-lock p-ml-2" />
                                    <label for="locked" class="kn-material-input-label p-ml-2"> {{ $t('common.locked') }} </label>
                                </span>
                            </form>
                        </template>
                    </Card>
                </div>
            </div>
        </div>
        <div class="p-col-5 p-m-0 p-p-0 p-d-flex p-flex-column kn-flex">
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    {{ $t('documentExecution.documentDetails.info.positionTitle') }}
                </template>
            </Toolbar>
            <div id="position-content" class="kn-flex kn-relative">
                <div :style="mainDescriptor.style.absoluteScroll">
                    <div id="restriction-container" class="p-m-2">
                        <Toolbar class="kn-toolbar kn-toolbar--default">
                            <template #start>
                                {{ $t('documentExecution.documentDetails.info.restrictionsTitle') }}
                            </template>
                        </Toolbar>
                        <Card>
                            <template #content>
                                <form class="p-formgrid p-grid p-mb-3">
                                    <span class="p-float-label p-col-10">
                                        <Textarea id="profiledVisibility" class="kn-material-input" rows="1" :autoResize="true" v-model="document.profiledVisibility" :disabled="true" />
                                        <label for="profiledVisibility" class="kn-material-input-label"> {{ $t('documentExecution.documentDetails.info.profiledVisibility') }} </label>
                                    </span>
                                    <Button icon="fas fa-plus-circle fa-1x" class="p-button-text p-button-plain p-ml-2 p-col-1" :disabled="!visibilityAttribute" @click="addRestriction" />
                                    <Button icon="fas fa-eraser fa-1x" class="p-button-text p-button-plain p-ml-2 p-col-1" @click="clearAllRestrictions" />
                                </form>
                                <form class="p-formgrid p-grid">
                                    <span class="p-field p-float-label p-col-12 p-lg-5">
                                        <Dropdown id="attributes" class="kn-material-input" v-model="visibilityAttribute" :options="availableAttributes" optionLabel="attributeName" optionValue="attributeName" />
                                        <label for="attributes" class="kn-material-input-label"> {{ $t('documentExecution.documentDetails.info.attribute') }} </label>
                                    </span>
                                    <span class="p-col-12 p-lg-1" :style="infoDescriptor.style.center">=</span>
                                    <span class="p-field p-float-label p-col-12 p-lg-6">
                                        <InputText id="restrictionValue" class="kn-material-input" v-model="restrictionValue" />
                                        <label for="restrictionValue" class="kn-material-input-label"> {{ $t('documentExecution.documentDetails.info.restrictionValueHint') }} </label>
                                    </span>
                                </form>
                            </template>
                        </Card>
                    </div>
                    <div id="tree-container" class="p-m-2">
                        <Toolbar class="kn-toolbar kn-toolbar--default">
                            <template #start>
                                {{ $t('documentExecution.documentDetails.info.visibilityLocationTitle') }}
                            </template>
                        </Toolbar>
                        <Card class="card-0-padding">
                            <template #content>
                                <DocumentDetailsTree :propFunctionalities="folders" :propSelectedFolders="document.functionalities" @selected="setFunctionality" />
                            </template>
                        </Card>
                    </div>
                </div>
            </div>
        </div>
        <DatasetDialog v-if="showDatasetDialog" :selectedDataset="selectedDataset" :visible="showDatasetDialog" @closeDialog="showDatasetDialog = false" @saveSelectedDataset="saveSelectedDataset" />
    </div>
</template>

<script lang="ts">
    import { iDocument, iDataSource, iEngine, iTemplate, iAttribute, iFolder } from '@/modules/documentExecution/documentDetails/DocumentDetails'
    import { defineComponent, PropType } from 'vue'
    import { createValidations } from '@/helpers/commons/validationHelper'
    import mainDescriptor from '../../DocumentDetailsDescriptor.json'
    import infoDescriptor from './DocumentDetailsInformationsDescriptor.json'
    import useValidate from '@vuelidate/core'
    import DatasetDialog from './DocumentDetailsDatasetDialog.vue'
    import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
    import Card from 'primevue/card'
    import Textarea from 'primevue/textarea'
    import Dropdown from 'primevue/dropdown'
    import InputSwitch from 'primevue/inputswitch'
    import KnInputFile from '@/components/UI/KnInputFile.vue'
    import DocumentDetailsTree from './DocumentDetailsTree.vue'

    export default defineComponent({
        name: 'document-details-informations',
        components: { DatasetDialog, Card, Textarea, Dropdown, InputSwitch, KnValidationMessages, KnInputFile, DocumentDetailsTree },
        props: {
            selectedDocument: { type: Object as PropType<iDocument> },
            selectedDataset: { type: Object },
            availableStates: { type: Array },
            selectedFolder: { type: Object as PropType<iFolder>, required: true },
            documentTypes: { type: Array as any, required: true },
            documentEngines: { type: Array as PropType<iEngine[]>, required: true },
            availableDatasources: { type: Array as PropType<iDataSource[]> },
            availableFolders: { type: Array as PropType<iFolder[]> },
            availableTemplates: { type: Array as PropType<iTemplate[]> },
            availableAttributes: { type: Array as PropType<iAttribute[]> }
        },
        emits: ['setTemplateForUpload', 'setImageForUpload', 'deleteImage', 'touched'],
        computed: {
            filteredEngines(): any {
                if (this.document.typeCode) {
                    return this.documentEngines.filter((engine) => engine.biobjTypeId === this.documentTypes.filter((type) => type.valueCd === this.document.typeCode)[0].valueId)
                }
                return []
            },
            isDataSourceVisible(): boolean {
                switch (this.document.engine) {
                    case 'knowageofficeengine':
                    case 'knowagecompositedoce':
                    case 'knowageprocessengine':
                    case 'knowagechartengine':
                    case 'knowagenetworkengine':
                    case 'knowagecockpitengine':
                    case 'knowagedossierengine':
                    case 'knowagekpiengine':
                    case 'knowagesvgviewerengine':
                        return false
                    default:
                        return true
                }
            },
            isDataSetVisible(): boolean {
                switch (this.document.engine) {
                    case 'knowagegisengine':
                    case 'knowagechartengine':
                    case 'knowagenetworkengine':
                        return true
                    default:
                        return false
                }
            },
            getImageUrl(): string {
                return process.env.VUE_APP_HOST_URL + `/knowage/servlet/AdapterHTTP?ACTION_NAME=MANAGE_PREVIEW_FILE_ACTION&SBI_ENVIRONMENT=DOCBROWSER&LIGHT_NAVIGATOR_DISABLED=TRUE&operation=DOWNLOAD&fileName=${this.selectedDocument?.previewFile}`
            }
        },
        data() {
            return {
                v$: useValidate() as any,
                mainDescriptor,
                infoDescriptor,
                uploading: false,
                lockedByUser: false,
                triggerUpload: false,
                showDatasetDialog: false,
                triggerImageUpload: false,
                dataset: {} as any,
                folders: [] as iFolder[],
                document: {} as iDocument,
                templates: [] as iTemplate[],
                templateToUpload: { name: '' } as any,
                imageToUpload: { name: '' } as any,
                visibilityAttribute: '',
                restrictionValue: ''
            }
        },
        created() {
            this.setData()
        },
        watch: {
            document() {
                this.setData()
            }
        },
        validations() {
            const validationObject = { document: createValidations('document', infoDescriptor.validations.document) }
            return validationObject
        },
        methods: {
            setData() {
                this.templates = this.availableTemplates as iTemplate[]
                this.document = this.selectedDocument as iDocument
                this.dataset = this.selectedDataset
                this.folders = this.availableFolders as iFolder[]
                this.IsLockedByUser()
            },
            IsLockedByUser() {
                this.lockedByUser = this.document.lockedByUser === 'true' ? true : false
            },
            setIsLockedByUser() {
                this.document.lockedByUser = this.lockedByUser ? 'true' : 'false'
            },
            addRestriction() {
                if (this.document.profiledVisibility) {
                    this.document.profiledVisibility = this.document.profiledVisibility + ' AND ' + this.visibilityAttribute + ' = ' + this.restrictionValue
                } else {
                    this.document.profiledVisibility = this.visibilityAttribute + ' = ' + this.restrictionValue
                }
            },
            clearAllRestrictions() {
                this.document.profiledVisibility = ''
                this.visibilityAttribute = ''
                this.restrictionValue = ''
            },
            saveSelectedDataset(event) {
                this.document.dataSetId = event.id
                this.dataset = event
            },
            setUploadType() {
                this.triggerUpload = false
                setTimeout(() => (this.triggerUpload = true), 200)
            },
            setTemplateForUpload(event) {
                this.uploading = true
                this.templateToUpload = event.target.files[0]
                this.$emit('setTemplateForUpload', event.target.files[0])
                this.triggerUpload = false
                setTimeout(() => (this.uploading = false), 200)
            },
            setImageUploadType() {
                this.triggerImageUpload = false
                setTimeout(() => (this.triggerImageUpload = true), 200)
            },
            setImageForUpload(event) {
                this.uploading = true
                this.imageToUpload = event.target.files[0]
                this.$emit('setImageForUpload', event.target.files[0])
                this.triggerImageUpload = false
                setTimeout(() => (this.uploading = false), 200)
            },
            setFunctionality(event) {
                this.document.functionalities = event
            },
            onTypeChange() {
                this.$emit('touched')
                this.document.engine = ''
            },
            openDesignerConfirm() {
                this.$confirm.require({
                    header: this.$t('common.toast.warning'),
                    message: this.$t('documentExecution.olap.openDesignerMsg'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => this.openDesigner()
                })
            },
            openDesigner() {
                this.$router.push(`/olap-designer/${this.document.id}`)
            }
        }
    })
</script>
<style lang="scss">
    .card-0-padding .p-card-body,
    .card-0-padding .p-card-content {
        padding: 0px;
    }
</style>
