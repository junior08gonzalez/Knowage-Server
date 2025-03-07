<template>
    <div class="p-grid p-m-0 kn-flex">
        <div class="p-col-4 p-sm-4 p-md-3 p-p-0 p-d-flex p-flex-column kn-flex">
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    {{ $t('documentExecution.documentDetails.history.listTitle') }}
                </template>
                <template #end>
                    <Button :label="$t('common.add')" class="p-button-text p-button-rounded p-button-plain kn-white-color" @click="setUploadType" />
                    <KnInputFile label="" v-if="!uploading" :changeFunction="startTemplateUpload" :triggerInput="triggerUpload" />
                </template>
            </Toolbar>
            <div id="drivers-list-container" class="kn-flex kn-relative">
                <div :style="mainDescriptor.style.absoluteScroll">
                    <ProgressBar v-if="loading" class="kn-progress-bar" mode="indeterminate" data-test="progress-bar" />
                    <KnListBox
                        v-if="!loading"
                        class="kn-height-full"
                        :options="listOfTemplates"
                        :settings="historyDescriptor.knListSettings"
                        @click="selectTemplate($event.item)"
                        @setActive.stop="setActiveTemplate($event.item)"
                        @download.stop="downloadTemplate($event.item)"
                        @delete.stop="deleteTemplateConfirm($event.item)"
                    ></KnListBox>
                </div>
            </div>
        </div>
        <div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0" :style="mainDescriptor.style.driverDetailsContainer">
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    {{ $t('documentExecution.documentDetails.history.template') }}
                </template>
                <template #end>
                    <Button :label="$t('documentExecution.olap.openDesigner')" class="p-button-text p-button-rounded p-button-plain kn-white-color" @click="openDesignerConfirm" />
                </template>
            </Toolbar>
            <div id="driver-details-container" class="kn-flex kn-relative">
                <div :style="mainDescriptor.style.absoluteScroll">
                    <VCodeMirror v-if="showTemplateContent" ref="codeMirrorScriptType" :style="mainDescriptor.style.height100" v-model:value="selectedTemplateContent" :options="scriptOptions" @keyup="$emit('touched')" />
                    <div v-else>
                        <InlineMessage severity="info" class="p-m-2"> {{ $t('documentExecution.documentDetails.history.templateHint') }}</InlineMessage>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
    import { iTemplate } from '@/modules/documentExecution/documentDetails/DocumentDetails'
    import { defineComponent, PropType } from 'vue'
    import { AxiosResponse } from 'axios'
    import { iDocument } from '@/modules/documentExecution/documentDetails/DocumentDetails'
    import { downloadDirect } from '@/helpers/commons/fileHelper'
    import { VCodeMirror } from 'vue3-code-mirror'
    import mainDescriptor from '@/modules/documentExecution/documentDetails/DocumentDetailsDescriptor.json'
    import driversDescriptor from '@/modules/documentExecution/documentDetails/tabs/drivers/DocumentDetailsDriversDescriptor.json'
    import historyDescriptor from './DocumentDetailsHistory.json'
    import KnListBox from '@/components/UI/KnListBox/KnListBox.vue'
    import KnInputFile from '@/components/UI/KnInputFile.vue'
    import InlineMessage from 'primevue/inlinemessage'

    export default defineComponent({
        name: 'document-drivers',
        components: { KnListBox, KnInputFile, VCodeMirror, InlineMessage },
        props: { selectedDocument: { type: Object as PropType<iDocument>, required: true } },
        computed: {
            showTemplateContent(): any {
                switch (this.selectedTemplateFileType) {
                    case 'xml':
                        return true
                    case 'xls':
                        return true
                    case 'rptdesign':
                        return true
                    case 'sbicockpit':
                        return true
                    case 'json': {
                        return true
                    }
                    default:
                        return false
                }
            }
        },
        data() {
            return {
                mainDescriptor,
                driversDescriptor,
                historyDescriptor,
                selectedTemplate: {} as iTemplate,
                listOfTemplates: [] as iTemplate[],
                selectedTemplateFileType: null as any,
                selectedTemplateContent: '' as any,
                loading: false,
                triggerUpload: false,
                uploading: false,
                codeMirrorScriptType: {} as any,
                tst: '',
                scriptOptions: {
                    readOnly: true,
                    mode: 'text/javascript',
                    indentWithTabs: true,
                    smartIndent: true,
                    lineWrapping: true,
                    matchBrackets: true,
                    autofocus: true,
                    theme: 'eclipse',
                    lineNumbers: true
                }
            }
        },
        created() {
            this.getAllTemplates()
            this.setupCodeMirror()
        },
        methods: {
            async getAllTemplates() {
                this.loading = true
                this.$http
                    .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${this.selectedDocument.id}/templates`)
                    .then((response: AxiosResponse<any>) => (this.listOfTemplates = response.data as iTemplate[]))
                    .finally(() => (this.loading = false))
            },
            async getSelectedTemplate(templateId) {
                this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${this.selectedDocument.id}/templates/selected/${templateId}`, { headers: { Accept: 'application/json, text/plain, */*', 'X-Disable-Errors': 'true' } }).then((response: AxiosResponse<any>) => {
                    this.selectedTemplateFileType == 'sbicockpit' || this.selectedTemplateFileType == 'json' ? (this.selectedTemplateContent = JSON.stringify(response.data, null, 4)) : (this.selectedTemplateContent = response.data)
                })
            },
            setupCodeMirror() {
                const interval = setInterval(() => {
                    if (!this.$refs.codeMirrorScriptType) return
                    this.codeMirrorScriptType = (this.$refs.codeMirrorScriptType as any).editor as any
                    clearInterval(interval)
                }, 200)
            },
            changeCodemirrorMode() {
                let mode = ''
                switch (this.selectedTemplateFileType) {
                    case 'xml':
                        mode = 'text/html'
                        break
                    case 'xls':
                        mode = 'text/html'
                        break
                    case 'rptdesign':
                        mode = 'text/html'
                        break
                    case 'sbicockpit':
                        mode = 'text/javascript'
                        break
                    case 'json': {
                        mode = 'text/javascript'
                    }
                }
                setTimeout(() => {
                    this.setupCodeMirror()
                    this.codeMirrorScriptType.setOption('mode', mode)
                }, 250)
            },
            setFileType(template) {
                if (template && template.name) {
                    let fileType = template.name.split('.')
                    this.selectedTemplateFileType = fileType[fileType.length - 1]
                }
            },
            selectTemplate(event) {
                this.selectedTemplate = event as iTemplate
                this.setFileType(event)
                this.changeCodemirrorMode()
                this.getSelectedTemplate(event.id)
            },
            setUploadType() {
                this.triggerUpload = false
                setTimeout(() => (this.triggerUpload = true), 200)
            },
            startTemplateUpload(event) {
                this.uploading = true
                this.uploadTemplate(event.target.files[0])
                this.triggerUpload = false
                setTimeout(() => (this.uploading = false), 200)
            },
            async uploadTemplate(uploadedFile) {
                var formData = new FormData()
                formData.append('file', uploadedFile)
                await this.$http
                    .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${this.selectedDocument.id}/templates`, formData, { headers: { 'Content-Type': 'multipart/form-data', 'X-Disable-Errors': 'true' } })
                    .then(() => {
                        this.$store.commit('setInfo', { title: this.$t('common.toast.success'), msg: this.$t('common.toast.uploadSuccess') })
                        this.getAllTemplates()
                    })
                    .catch(() => this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: this.$t('documentExecution.documentDetails.history.uploadError') }))
                    .finally(() => (this.triggerUpload = false))
            },
            setActiveTemplate(template) {
                this.$http
                    .put(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${this.selectedDocument.id}/templates/${template.id}`, { headers: { 'X-Disable-Errors': 'true' } })
                    .then(() => {
                        this.$store.commit('setInfo', { title: this.$t('common.toast.success'), msg: this.$t('documentExecution.documentDetails.history.activeOk') })
                        this.getAllTemplates()
                    })
                    .catch(() => this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: this.$t('documentExecution.documentDetails.history.activeError') }))
            },
            async downloadTemplate(template) {
                let fileType = template.name.split('.')
                await this.$http
                    .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${this.selectedDocument.id}/templates/${template.id}/${fileType[fileType.length - 1]}/file`, {
                        headers: { Accept: 'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9', 'X-Disable-Errors': 'true' }
                    })
                    .then((response: AxiosResponse<any>) => {
                        if (response.data.errors) {
                            this.$store.commit('setError', { title: this.$t('common.error.downloading'), msg: this.$t('common.error.errorCreatingPackage') })
                        } else {
                            this.$store.commit('setInfo', { title: this.$t('common.toast.success') })
                            if (response.headers) {
                                var contentDisposition = response.headers['content-disposition']
                                var contentDispositionMatcher = contentDisposition.match(/filename[^;\n=]*=((['"]).*?\2|[^;\n]*)/i)
                                if (contentDispositionMatcher && contentDispositionMatcher.length > 1) {
                                    var fileAndExtension = contentDispositionMatcher[1]
                                    var completeFileName = fileAndExtension.replaceAll('"', '')
                                    if (fileType[fileType.length - 1] == 'json' || fileType[fileType.length - 1] == 'sbicockpit') {
                                        downloadDirect(JSON.stringify(response.data), completeFileName, 'text/html; charset=UTF-8')
                                    } else {
                                        downloadDirect(response.data, completeFileName, 'text/html; charset=UTF-8')
                                    }
                                }
                            }
                        }
                    })
                    .catch((error) => this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: error.message }))
            },
            deleteTemplateConfirm(template) {
                this.$confirm.require({
                    header: this.$t('common.toast.deleteConfirmTitle'),
                    message: this.$t('common.toast.deleteMessage'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => this.deleteTemplate(template)
                })
            },
            async deleteTemplate(template) {
                await this.$http
                    .delete(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${this.selectedDocument.id}/templates/${template.id}`, { headers: { Accept: 'application/json, text/plain, */*', 'X-Disable-Errors': 'true' } })
                    .then(() => {
                        this.$store.commit('setInfo', { title: this.$t('common.toast.deleteTitle'), msg: this.$t('common.toast.deleteSuccess') })
                        this.getAllTemplates()
                    })
                    .catch((error) => this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: error.message }))
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
                this.$router.push(`/olap-designer/${this.selectedDocument.id}`)
            }
        }
    })
</script>
