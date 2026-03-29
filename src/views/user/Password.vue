<template>
  <div class="user-password">
    <div class="card">
      <div class="card-header">
        <span class="card-title">修改密码</span>
      </div>

      <el-form ref="formRef" :model="passwordForm" :rules="passwordRules" label-width="100px" class="password-form">
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入当前密码"
            show-password
          />
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码（6-20位）"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSave">保存修改</el-button>
        </el-form-item>
      </el-form>

      <el-alert
        title="密码安全提示"
        type="info"
        :closable="false"
        show-icon
        class="mt-20"
      >
        <p>1. 密码长度为6-20位</p>
        <p>2. 建议使用字母、数字和特殊字符的组合</p>
        <p>3. 请勿使用与其他网站相同的密码</p>
        <p>4. 定期修改密码可以提高账户安全性</p>
      </el-alert>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { useUserStore } from '@/stores/user'
import userApi from '@/api/user'
import { isValidPassword } from '@/utils/validate'

const router = useRouter()
const authStore = useAuthStore()
const userStore = useUserStore()
const loading = ref(false)
const formRef = ref(null)

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateNewPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入新密码'))
  } else if (!isValidPassword(value)) {
    callback(new Error('密码长度为6-20位'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入新密码'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [{ required: true, validator: validateNewPassword, trigger: 'blur' }],
  confirmPassword: [{ required: true, validator: validateConfirmPassword, trigger: 'blur' }]
}

const handleSave = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const res = await userApi.updatePassword({
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      })
      if (res.code === 0) {
        ElMessage.success('密码修改成功，请重新登录')
        authStore.clearAuth()
        userStore.clearUser()
        router.push('/login')
      }
    } catch (error) {
      console.error('修改密码失败:', error)
    } finally {
      loading.value = false
    }
  })
}
</script>

<style lang="scss" scoped>
.user-password {
  padding: 0;
}

.password-form {
  max-width: 500px;
}

.el-alert {
  p {
    margin: 4px 0;
    font-size: 13px;
    color: #606266;
  }
}
</style>
