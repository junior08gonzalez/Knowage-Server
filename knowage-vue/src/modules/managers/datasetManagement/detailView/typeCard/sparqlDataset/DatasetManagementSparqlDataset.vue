<template>
    <Card class="p-mt-3">
        <template #content>
            <form class="p-fluid p-formgrid p-grid">
                <div class="p-field p-col-12">
                    <span class="p-float-label">
                        <InputText
                            id="sparqlEndpoint"
                            class="kn-material-input"
                            type="text"
                            v-model.trim="v$.dataset.sparqlEndpoint.$model"
                            :class="{
                                'p-invalid': v$.dataset.sparqlEndpoint.$invalid && v$.dataset.sparqlEndpoint.$dirty
                            }"
                            @blur="v$.dataset.sparqlEndpoint.$touch()"
                            @change="$emit('touched')"
                        />
                        <label for="sparqlEndpoint" class="kn-material-input-label"> {{ $t('managers.datasetManagement.sparqlEndpoint') }} * </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :vComp="v$.dataset.sparqlEndpoint" :additionalTranslateParams="{ fieldName: $t('managers.datasetManagement.sparqlEndpoint') }" />
                </div>
                <div class="p-field p-col-6">
                    <span class="p-float-label">
                        <InputText id="sparqlDefaultGraphIri" class="kn-material-input" v-model.trim="dataset.sparqlDefaultGraphIri" @change="$emit('touched')" />
                        <label for="sparqlDefaultGraphIri" class="kn-material-input-label"> {{ $t('managers.datasetManagement.sparqlDefaultGraphIri') }} </label>
                    </span>
                </div>
                <div class="p-field p-col-6">
                    <span class="p-float-label">
                        <InputText id="sparqlExecutionTimeout" class="kn-material-input" type="number" v-model.trim="dataset.sparqlExecutionTimeout" @change="$emit('touched')" />
                        <label for="sparqlExecutionTimeout" class="kn-material-input-label"> {{ $t('managers.datasetManagement.sparqlExecutionTimeout') }} </label>
                    </span>
                </div>
            </form>
            <VCodeMirror class="p-mt-2" ref="codeMirrorSparql" v-model:value="dataset.sparqlQuery" :autoHeight="true" :options="sparqlOptions" @keyup="$emit('touched')" />
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { createValidations, ICustomValidatorMap } from '@/helpers/commons/validationHelper'
import { VCodeMirror } from 'vue3-code-mirror'
import useValidate from '@vuelidate/core'
import sparqlDescriptor from './DatasetManagementSparqlDataset.json'
import Card from 'primevue/card'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'

export default defineComponent({
    components: { Card, VCodeMirror, KnValidationMessages },
    props: { selectedDataset: { type: Object as any } },
    emits: ['touched'],
    data() {
        return {
            sparqlDescriptor,
            dataset: {} as any,
            v$: useValidate() as any,
            codeMirrorSparql: {} as any,
            sparqlOptions: {
                mode: 'application/sparql-query',
                lineWrapping: true,
                indentWithTabs: true,
                smartIndent: true,
                matchBrackets: true,
                theme: 'eclipse',
                lineNumbers: true
            }
        }
    },
    created() {
        this.loadDataset()
        this.setupCodeMirror()
    },
    watch: {
        selectedDataset() {
            this.loadDataset()
        }
    },
    validations() {
        const sparqlFieldsRequired = (value) => {
            return this.dataset.dsTypeCd != 'SPARQL' || value
        }
        const customValidators: ICustomValidatorMap = { 'sparql-fields-required': sparqlFieldsRequired }
        const validationObject = { dataset: createValidations('dataset', sparqlDescriptor.validations.dataset, customValidators) }
        return validationObject
    },
    methods: {
        loadDataset() {
            this.dataset = this.selectedDataset
            this.dataset.sparqlQuery ? '' : (this.dataset.sparqlQuery = '')
        },
        setupCodeMirror() {
            this.$refs.codeMirrorSparql ? (this.codeMirrorSparql = (this.$refs.codeMirrorSparql as any).editor as any) : ''
        }
    }
})
</script>
