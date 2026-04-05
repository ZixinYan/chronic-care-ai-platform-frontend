<template>
  <div class="upload-report">
    <div class="card">
      <div class="card-header">
        <span class="card-title">上传健康报告</span>
      </div>

      <el-form
        ref="formRef"
        :model="uploadForm"
        :rules="uploadRules"
        label-width="100px"
        class="upload-form"
      >
        <el-form-item label="报告标题" prop="title">
          <el-input v-model="uploadForm.title" placeholder="请输入报告标题" maxlength="100" />
        </el-form-item>

        <el-form-item label="报告类型" prop="reportType">
          <el-radio-group v-model="uploadForm.reportType">
            <el-radio :label="1">图片报告</el-radio>
            <el-radio :label="2">文字报告</el-radio>
            <el-radio :label="3">PDF报告</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="报告分类" prop="category">
          <el-select v-model="uploadForm.category" placeholder="请选择分类">
            <el-option label="血糖检测" value="blood_sugar" />
            <el-option label="血压检测" value="blood_pressure" />
            <el-option label="心电图" value="ecg" />
            <el-option label="血常规" value="blood_routine" />
            <el-option label="尿常规" value="urine_routine" />
            <el-option label="体检报告" value="physical_examination" />
            <el-option label="影像检查" value="imaging" />
            <el-option label="病理报告" value="pathology" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>

        <el-form-item v-if="uploadForm.reportType === 2" label="报告内容" prop="textContent">
          <el-input
            v-model="uploadForm.textContent"
            type="textarea"
            :rows="8"
            placeholder="请输入报告内容"
            maxlength="5000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item v-if="uploadForm.reportType !== 2" label="上传文件" prop="file">
          <el-upload
            ref="uploadRef"
            class="upload-area"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :on-exceed="handleExceed"
            :before-upload="beforeUpload"
            :file-list="fileList"
            :accept="acceptTypes"
          >
            <el-icon class="upload-icon"><UploadFilled /></el-icon>
            <div class="upload-text">
              将文件拖到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="upload-tip">
                {{ uploadTip }}
              </div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item label="备注">
          <el-input
            v-model="uploadForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">
            提交上传
          </el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import healthReportApi from '@/api/healthReport'
import { isValidFileSize, isValidImageType, isValidPdfType } from '@/utils/validate'

const router = useRouter()
const formRef = ref(null)
const uploadRef = ref(null)
const loading = ref(false)
const fileList = ref([])
const selectedFile = ref(null)

const uploadForm = reactive({
  title: '',
  reportType: 1,
  category: '',
  textContent: '',
  remark: ''
})

const uploadRules = {
  title: [
    { required: true, message: '请输入报告标题', trigger: 'blur' }
  ],
  reportType: [
    { required: true, message: '请选择报告类型', trigger: 'change' }
  ],
  category: [
    { required: true, message: '请选择报告分类', trigger: 'change' }
  ],
  textContent: [
    { required: true, message: '请输入报告内容', trigger: 'blur' }
  ]
}

const acceptTypes = computed(() => {
  return uploadForm.reportType === 1 ? '.jpg,.jpeg,.png' : '.pdf'
})

const uploadTip = computed(() => {
  if (uploadForm.reportType === 1) {
    return '支持JPG、PNG格式图片，大小不超过10MB'
  }
  return '支持PDF格式文件，大小不超过10MB'
})

const beforeUpload = (file) => {
  if (!isValidFileSize(file, 10)) {
    ElMessage.error('文件大小不能超过10MB')
    return false
  }

  if (uploadForm.reportType === 1 && !isValidImageType(file)) {
    ElMessage.error('请上传JPG或PNG格式的图片')
    return false
  }

  if (uploadForm.reportType === 3 && !isValidPdfType(file)) {
    ElMessage.error('请上传PDF格式的文件')
    return false
  }

  return true
}

const handleFileChange = (file) => {
  if (beforeUpload(file.raw)) {
    selectedFile.value = file.raw
  } else {
    fileList.value = []
    selectedFile.value = null
  }
}

const handleExceed = () => {
  ElMessage.warning('只能上传一个文件')
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    if (uploadForm.reportType !== 2 && !selectedFile.value) {
      ElMessage.error('请选择要上传的文件')
      return
    }

    if (uploadForm.reportType === 2 && !uploadForm.textContent.trim()) {
      ElMessage.error('请输入报告内容')
      return
    }

    loading.value = true
    try {
      if (uploadForm.reportType === 2) {
        const res = await healthReportApi.saveTextReport({
          title: uploadForm.title,
          category: uploadForm.category,
          textContent: uploadForm.textContent,
          description: uploadForm.remark
        })
        if (res.code === 0) {
          ElMessage.success('上传成功')
          router.push('/patient/health-report')
        }
      } else {
        const res = await healthReportApi.uploadReport(
          {
            title: uploadForm.title,
            reportType: uploadForm.reportType,
            category: uploadForm.category,
            description: uploadForm.remark
          },
          selectedFile.value,
          (progress) => {
            console.log('上传进度:', progress)
          }
        )
        if (res.code === 0) {
          ElMessage.success('上传成功')
          router.push('/patient/health-report')
        }
      }
    } catch (error) {
      console.error('上传失败:', error)
    } finally {
      loading.value = false
    }
  })
}

const handleCancel = () => {
  router.back()
}
</script>

<style lang="scss" scoped>
.upload-report {
  padding: 0;
}

.upload-form {
  max-width: 600px;
}

.upload-area {
  width: 100%;

  :deep(.el-upload) {
    width: 100%;
    border: 2px dashed #dcdfe6;
    border-radius: 8px;
    background-color: #fafafa;
    transition: all 0.3s;

    &:hover {
      border-color: #409eff;
    }
  }

  :deep(.el-upload-dragger) {
    width: 100%;
    height: 180px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    border: none;
    background-color: transparent;
  }
}

.upload-icon {
  font-size: 48px;
  color: #c0c4cc;
  margin-bottom: 16px;
}

.upload-text {
  font-size: 14px;
  color: #606266;

  em {
    color: #409eff;
    font-style: normal;
  }
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}
</style>
