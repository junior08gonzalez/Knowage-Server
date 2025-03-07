<template>
    <div class="managerDetail">
        <Dialog class="kn-dialog--toolbar--primary knMetadataDialog" v-bind:visible="visibility" footer="footer" :header="$t('managers.resourceManagement.metadata.dialog.title')" :closable="false" modal @id="loadMetadata">
            <div class="p-grid p-m-3 p-fluid p-ai-start">
                <span class="p-float-label p-col-4">
                    <InputText id="name" class="kn-material-input" type="text" v-model="metadata.name" @change="setDirty" />
                    <label class="kn-material-input-label" for="name">{{ $t('common.name') }}</label>
                </span>

                <span class="p-float-label p-col-2">
                    <InputText id="name" class="kn-material-input" type="text" v-model="metadata.version" @change="setDirty" />
                    <label class="kn-material-input-label" for="name">{{ $t(descriptor.metadata.version.label) }}</label>
                </span>

                <span class="p-float-label p-col-3">
                    <Dropdown id="typeOfAnalytics" class="kn-material-input" v-model="metadata.typeOfAnalytics" @change="setDirty" :options="translatedOptions" optionLabel="name" optionValue="value"> </Dropdown>
                    <label class="kn-material-input-label" for="outputType">{{ $t(descriptor.metadata.typeOfAnalytics.label) }}</label>
                </span>
                <span class="p-col-3 p-d-flex p-jc-end">
                    <label class="kn-material-input-label p-mr-2" for="name">{{ $t(descriptor.metadata.openSource.label) }}</label>
                    <InputSwitch v-model="metadata.openSource" @change="setDirty" />
                </span>
                <span class="p-float-label p-col-8 descriptionTextArea">
                    <Textarea v-model="metadata.description" class="kn-material-input " style="resize:none" id="description" rows="5" @change="setDirty" />
                    <label class="kn-material-input-label" for="description">{{ $t('common.description') }}</label>
                </span>
                <span class="p-col-4 kn-height-full">
                    <span class="p-d-flex p-jc-end p-ai-center">
                        <input id="inputImage" type="file" @change="uploadFile" accept="image/png, image/jpeg" />
                        <label for="inputImage" v-tooltip.bottom="$t('common.upload')">
                            <i class="p-button-text p-button-sm p-button-rounded p-button-plain p-p-0 pi pi-upload" />
                        </label>
                        <i class="p-button-text p-button-sm p-button-rounded p-button-plain p-p-0 pi pi-times p-ml-2" v-if="metadata.image" @click="removeImage" v-tooltip.bottom="$t('common.delete')" />
                    </span>
                    <div class="imageContainer p-d-flex p-jc-center p-ai-center">
                        <i class="far fa-image fa-5x icon" v-if="!metadata.image" />
                        <img :src="metadata.image" v-if="metadata.image" height="100%" class="kn-no-select" />
                    </div>
                </span>
            </div>

            <Accordion class="p-col-12">
                <AccordionTab :header="$t(descriptor.metadata.accuracyAndPerformance.label)">
                    <Textarea v-model="metadata.accuracyAndPerformance" class="kn-material-input metadataTextArea" style="resize:none" id="accuracyAndPerformance" rows="3" @change="setDirty" />
                </AccordionTab>
                <AccordionTab :header="$t(descriptor.metadata.usageOfTheModel.label)">
                    <Textarea v-model="metadata.usageOfTheModel" class="kn-material-input metadataTextArea" style="resize:none" id="usageOfTheModel" rows="3" @change="setDirty" />
                </AccordionTab>
                <AccordionTab :header="$t(descriptor.metadata.formatOfData.label)">
                    <Textarea v-model="metadata.formatOfData" class="kn-material-input metadataTextArea" style="resize:none" id="formatOfData" rows="3" @change="setDirty" />
                </AccordionTab>
            </Accordion>

            <template #footer>
                <Button class="kn-button kn-button--secondary" @click="closeDialog">{{ $t('common.close') }} </Button>
                <Button class="kn-button kn-button--primary" @click="saveMetadata" :disabled="!dirty"> {{ $t('common.save') }}</Button>
            </template>
        </Dialog>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import Dialog from 'primevue/dialog'
import Dropdown from 'primevue/dropdown'
import { mapState } from 'vuex'
import InputSwitch from 'primevue/inputswitch'
import resourceManagementDescriptor from './ResourceManagementDescriptor.json'
import Textarea from 'primevue/textarea'

import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'
import { iModelMetadataTemplate } from './ResourceManagement'

export default defineComponent({
    name: 'metadata-dialog',
    components: { Dialog, Dropdown, InputSwitch, Accordion, AccordionTab, Textarea },
    data() {
        return {
            dirty: false,
            loading: true,
            metadata: {} as iModelMetadataTemplate,
            checked: false,
            descriptor: resourceManagementDescriptor,
            translatedOptions: Array<any>()
        }
    },
    created() {
        this.loadMetadata()
        let notTranslatedOptions = this.descriptor.metadata.typeOfAnalytics.options
        for (var idx in notTranslatedOptions) {
            let translatedOption = notTranslatedOptions[idx]
            translatedOption.name = this.$t(translatedOption.name)
            this.translatedOptions.push(translatedOption)
        }
    },
    props: {
        id: String,
        visibility: Boolean
    },
    emits: ['update:visibility'],
    methods: {
        closeDialog() {
            this.$emit('update:visibility', false)
        },
        loadMetadata() {
            this.loading = true

            if (this.id) {
                this.$http
                    .get(process.env.VUE_APP_API_PATH + `2.0/resources/files/metadata?key=` + this.id)
                    .then((response: AxiosResponse<any>) => {
                        this.metadata = response.data
                    })
                    .catch(() => {
                        this.$store.commit('setError', {
                            title: this.$t('common.toast.metadata'),
                            msg: this.$t('common.toast.metadataLoadingFailed')
                        })
                    })
            }
            this.loading = false
        },
        removeImage() {
            this.metadata.image = ''
            this.setDirty()
        },
        saveMetadata(): void {
            this.loading = true
            if (this.id) {
                this.$http
                    .post(process.env.VUE_APP_API_PATH + `2.0/resources/files/metadata?key=` + this.id, this.metadata, {
                        responseType: 'arraybuffer', // important...because we need to convert it to a blob. If we don't specify this, response.data will be the raw data. It cannot be converted to blob directly.

                        headers: {
                            'Content-Type': 'application/json'
                        }
                    })
                    .then(() => {
                        this.$store.commit('setInfo', {
                            title: this.$t('common.toast.updateTitle'),
                            msg: this.$t('common.toast.updateSuccess')
                        })
                    })
                    .catch((error) => {
                        this.$store.commit('setError', {
                            title: this.$t('common.toast.updateTitle'),
                            msg: this.$t(error)
                        })
                    })
            }
            this.loading = false
            this.closeDialog()
        },
        setDirty(): void {
            this.dirty = true
        },
        uploadFile(event): void {
            const reader = new FileReader()
            let self = this
            reader.addEventListener(
                'load',
                function() {
                    self.metadata.image = reader.result || ''
                },
                false
            )
            if (event.srcElement.files[0] && event.srcElement.files[0].size < process.env.VUE_APP_MAX_UPLOAD_IMAGE_SIZE) {
                reader.readAsDataURL(event.srcElement.files[0])
                this.setDirty()
            } else this.$store.commit('setError', { title: this.$t('common.error.uploading'), msg: this.$t('common.error.exceededSize', { size: '(200KB)' }) })
        }
    },
    computed: {
        ...mapState({
            locale: 'locale'
        })
    },
    watch: {
        id() {
            this.loadMetadata()
        }
    }
})
</script>

<style scoped lang="scss">
#inputImage {
    display: none;
}
.knMetadataDialog {
    &:deep(.p-dialog-content) {
        width: 600px;
        min-width: 600px;
        max-width: 1200px;

        height: 600px;
        min-height: 600px;
        max-height: 900px;
    }

    .p-fileupload-buttonbar {
        border: none;

        .p-button:not(.p-fileupload-choose) {
            display: none;
        }

        .p-fileupload-choose {
            @extend .kn-button--primary;
        }
    }

    .metadataTextArea {
        width: 100%;
    }
    .imageContainer {
        height: 100%;
        .icon {
            color: var(--kn-color-secondary);
        }
        img {
            height: 150px;
            max-width: 300px;
        }
    }

    .descriptionTextArea {
        height: 200px;

        &:deep(.p-inputtextarea) {
            height: 200px;
        }
    }
}
</style>
