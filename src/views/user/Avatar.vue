<template>
  <div class="user-avatar">
    <div class="card">
      <div class="card-header">
        <span class="card-title">修改头像</span>
      </div>

      <div class="avatar-content">
        <div class="current-avatar">
          <h4>当前头像</h4>
          <el-avatar :size="120" :src="avatarUrl">
            <el-icon><UserFilled /></el-icon>
          </el-avatar>
        </div>

        <div class="avatar-upload">
          <h4>上传新头像</h4>
          <el-upload
            class="avatar-uploader"
            :show-file-list="false"
            :before-upload="beforeUpload"
            :http-request="handleUpload"
          >
            <el-avatar v-if="previewAvatar" :size="120" :src="previewAvatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tips">
            <p>支持JPG、PNG格式</p>
            <p>文件大小不超过2MB</p>
            <p>建议尺寸：200x200像素</p>
          </div>
        </div>

        <div v-if="previewAvatar" class="avatar-actions">
          <el-button type="primary" :loading="loading" @click="saveAvatar">保存头像</el-button>
          <el-button @click="cancelPreview">取消</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { UserFilled, Plus } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import authApi from '@/api/auth'
import { isValidImageType, isValidFileSize } from '@/utils/validate'

const userStore = useUserStore()
const loading = ref(false)
const previewAvatar = ref('')
const selectedFile = ref(null)
const avatarTimestamp = ref(Date.now())

const avatarUrl = computed(() => {
  const avatar = userStore.avatar
  if (!avatar) return ''
  return avatar.includes('?') ? `${avatar}&t=${avatarTimestamp.value}` : `${avatar}?t=${avatarTimestamp.value}`
})

const beforeUpload = (file) => {
  if (!isValidImageType(file)) {
    ElMessage.error('请上传JPG或PNG格式的图片')
    return false
  }
  if (!isValidFileSize(file, 2)) {
    ElMessage.error('图片大小不能超过2MB')
    return false
  }
  return true
}

const handleUpload = (options) => {
  const file = options.file
  selectedFile.value = file

  const reader = new FileReader()
  reader.onload = (e) => {
    previewAvatar.value = e.target.result
  }
  reader.readAsDataURL(file)
}

const saveAvatar = async () => {
  if (!selectedFile.value) return

  loading.value = true
  try {
    const res = await authApi.uploadAvatar(selectedFile.value)
    if (res.code === 0) {
      userStore.updateUserInfo({ avatar: res.data })
      avatarTimestamp.value = Date.now()
      ElMessage.success('头像修改成功')
      cancelPreview()
    }
  } catch (error) {
    console.error('上传头像失败:', error)
  } finally {
    loading.value = false
  }
}

const cancelPreview = () => {
  previewAvatar.value = ''
  selectedFile.value = null
}
</script>

<style lang="scss" scoped>
.user-avatar {
  padding: 0;
}

.avatar-content {
  display: flex;
  gap: 60px;
  align-items: flex-start;

  h4 {
    font-size: 14px;
    color: #606266;
    margin-bottom: 16px;
  }
}

.current-avatar,
.avatar-upload {
  text-align: center;
}

.avatar-uploader {
  :deep(.el-upload) {
    border: 2px dashed #dcdfe6;
    border-radius: 50%;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      border-color: #409eff;
    }
  }
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-tips {
  margin-top: 16px;
  text-align: left;

  p {
    font-size: 12px;
    color: #909399;
    margin: 4px 0;
  }
}

.avatar-actions {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}
</style>
