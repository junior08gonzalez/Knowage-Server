<template>
    <Dialog class="kn-dialog--toolbar--primary importFileDialog" v-bind:visible="visibility" footer="footer" :header="$t('common.import')" :closable="false" modal>
        <span v-if="checkExistingFile()">
            <Message severity="warn">{{ $t('managers.resourceManagement.uploadFile.fileWillBeOverwritten') }}</Message>
        </span>
        <span v-if="notifyExtractionWarning()">
            <Message severity="warn">{{ $t('managers.resourceManagement.uploadFile.fileAndFoldersContentWillBeOverwritten') }}</Message>
        </span>
        <FileUpload name="demo[]" :chooseLabel="$t('common.choose')" :customUpload="true" @uploader="onUpload" @remove="onDelete" auto="true" :maxFileSize="50000000" :multiple="false" :fileLimit="1">
            <template #empty>
                <p>{{ $t('common.dragAndDropFileHere') }}</p>
            </template>
        </FileUpload>

        <span :class="notifyExtractionWarning() || checkExistingFile() ? 'inputFileToggleWithToolbar' : 'inputFileToggle'" v-if="isArchive()">
            <label for="active" class="kn-material-input-label p-mr-3"> {{ $t('managers.resourceManagement.extract') }}</label>
            <InputSwitch v-model="checked" />
        </span>
        <template #footer>
            <Button class="p-button-text kn-button" :label="$t('common.cancel')" @click="closeDialog" />
            <Button class="kn-button kn-button--primary" v-t="'common.import'" :disabled="uploadedFiles && uploadedFiles.length == 0" @click="startImportFile" />
        </template>
    </Dialog>
</template>

<script lang="ts">
    import { AxiosResponse } from 'axios'
    import { defineComponent } from 'vue'
    import Dialog from 'primevue/dialog'
    import FileUpload from 'primevue/fileupload'
    import InputSwitch from 'primevue/inputswitch'
    import resourceManagementDescriptor from './ResourceManagementDescriptor.json'
    import { PropType } from 'vue'
    import { IFileTemplate } from './ResourceManagement'
    import Message from 'primevue/message'

    export default defineComponent({
        name: 'import-file-dialog',
        components: { Dialog, FileUpload, InputSwitch, Message },
        props: {
            path: String,
            visibility: Boolean,
            existingFiles: Array as PropType<Array<IFileTemplate>>
        },
        data() {
            return { checked: false, descriptor: resourceManagementDescriptor, uploadedFiles: [], loading: false }
        },
        emits: ['update:visibility', 'fileUploaded'],
        methods: {
            isArchive() {
                // eslint-disable-next-line
                // @ts-ignore
                return this.uploadedFiles.length == 1 && (this.uploadedFiles[0].type == 'application/zip' || this.uploadedFiles[0].type == 'application/x-zip-compressed')
            },
            closeDialog(): void {
                this.uploadedFiles = []
                this.$emit('update:visibility', false)
            },
            onDelete(idx) {
                this.uploadedFiles.splice(idx)
            },
            onUpload(data) {
                // eslint-disable-next-line
                // @ts-ignore
                this.uploadedFiles[0] = data.files[0]
            },
            checkExistingFile() {
                let found = false
                if (this.existingFiles && this.uploadedFiles[0]) {
                    for (var idx in this.existingFiles) {
                        let element = this.existingFiles[idx]
                        // eslint-disable-next-line
                        // @ts-ignore
                        if (element.name === this.uploadedFiles[0].name) {
                            found = true
                            break
                        }
                    }
                }

                return found && !this.checked
            },
            notifyExtractionWarning() {
                return this.isArchive() && this.checked
            },
            async startImportFile() {
                if (this.uploadedFiles[0] && this.path) {
                    this.loading = true

                    var formData = new FormData()
                    formData.append('file', this.uploadedFiles[0])
                    formData.append('key', this.path)
                    let checkedAsString = this.checked ? 'true' : 'false'
                    formData.append('extract', checkedAsString)
                    await this.$http
                        .post(process.env.VUE_APP_API_PATH + '2.0/resources/files/uploadFile', formData, {
                            headers: {
                                'Content-Type': 'multipart/form-data'
                            }
                        })
                        .then(
                            (response: AxiosResponse<any>) => {
                                if (response.data.errors) {
                                    this.$store.commit('setError', { title: this.$t('common.error.uploading'), msg: this.$t('managers.resourceManagement.upload.completedWithErrors') })
                                } else {
                                    this.$store.commit('setInfo', { title: this.$t('common.uploading'), msg: this.$t('common.toast.uploadSuccess') })
                                }
                            },
                            (error) => this.$store.commit('setError', { title: this.$t('common.error.uploading'), msg: this.$t(error) })
                        )

                        .finally(() => {
                            this.loading = false
                            this.closeDialog()
                            this.uploadedFiles = []
                            this.$emit('fileUploaded')
                        })
                } else {
                    this.$store.commit('setWarning', { title: this.$t('common.uploading'), msg: this.$t('managers.widgetGallery.noFileProvided') })
                }
            }
        }
    })
</script>
<style lang="scss">
    .importFileDialog {
        position: relative;
        min-width: 600px;
        width: 60%;
        max-width: 1200px;

        .p-fileupload-buttonbar {
            border: none;

            .p-button:not(.p-fileupload-choose) {
                display: none;
            }

            .p-fileupload-choose {
                @extend .kn-button--primary;
            }
        }

        .p-dialog-content {
            position: relative;
        }

        .functionalityTable {
            min-height: 400px;
            height: 40%;
        }
    }

    .inputFileToggle {
        right: 20px;
        position: absolute;
        top: 20px;
    }
    .inputFileToggleWithToolbar {
        @extend .inputFileToggle;

        top: 80px;
    }
</style>
