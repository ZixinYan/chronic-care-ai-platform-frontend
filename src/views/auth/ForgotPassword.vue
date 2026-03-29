<template>
  <div class="forgot-container">
    <div class="forgot-box">
      <div class="forgot-header">
        <h2>找回密码</h2>
        <p>通过手机验证码重置您的密码</p>
      </div>

      <el-steps :active="activeStep" align-center class="steps">
        <el-step title="验证手机号" />
        <el-step title="设置新密码" />
        <el-step title="完成" />
      </el-steps>

      <el-form
        v-show="activeStep === 0"
        ref="phoneFormRef"
        :model="phoneForm"
        :rules="phoneRules"
        class="forgot-form"
      >
        <el-form-item prop="phone">
          <el-input
            v-model="phoneForm.phone"
            placeholder="请输入手机号"
            prefix-icon="Phone"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="code">
          <div class="code-input">
            <el-input
              v-model="phoneForm.code"
              placeholder="请输入验证码"
              prefix-icon="Message"
              size="large"
              maxlength="6"
            />
            <el-button
              :disabled="countdown > 0"
              size="large"
              @click="handleSendCode"
            >
              {{ countdown > 0 ? `${countdown}s后重发` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="submit-btn"
            @click="handleVerifyPhone"
          >
            下一步
          </el-button>
        </el-form-item>
      </el-form>

      <el-form
        v-show="activeStep === 1"
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        class="forgot-form"
      >
        <el-form-item prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码（6-20位）"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="submit-btn"
            @click="handleResetPassword"
          >
            确认重置
          </el-button>
        </el-form-item>
      </el-form>

      <div v-show="activeStep === 2" class="success-content">
        <el-result
          icon="success"
          title="密码重置成功"
          sub-title="请使用新密码登录"
        >
          <template #extra>
            <el-button type="primary" @click="goToLogin">
              去登录
            </el-button>
          </template>
        </el-result>
      </div>

      <div v-if="activeStep < 2" class="forgot-footer">
        <router-link to="/login">返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import authApi from '@/api/auth'
import { isValidPhone, isValidSmsCode, isValidPassword } from '@/utils/validate'

const router = useRouter()
const activeStep = ref(0)
const loading = ref(false)
const countdown = ref(0)

const phoneFormRef = ref(null)
const passwordFormRef = ref(null)

const phoneForm = reactive({
  phone: '',
  code: ''
})

const passwordForm = reactive({
  newPassword: '',
  confirmPassword: ''
})

const validatePhone = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入手机号'))
  } else if (!isValidPhone(value)) {
    callback(new Error('请输入正确的手机号'))
  } else {
    callback()
  }
}

const validateSmsCode = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入验证码'))
  } else if (!isValidSmsCode(value)) {
    callback(new Error('请输入6位数字验证码'))
  } else {
    callback()
  }
}

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

const phoneRules = {
  phone: [{ required: true, validator: validatePhone, trigger: 'blur' }],
  code: [{ required: true, validator: validateSmsCode, trigger: 'blur' }]
}

const passwordRules = {
  newPassword: [{ required: true, validator: validateNewPassword, trigger: 'blur' }],
  confirmPassword: [{ required: true, validator: validateConfirmPassword, trigger: 'blur' }]
}

let timer = null

const handleSendCode = async () => {
  if (!phoneForm.phone || !isValidPhone(phoneForm.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }

  try {
    const res = await authApi.sendSmsCode(phoneForm.phone)
    if (res.code === 0) {
      ElMessage.success('验证码已发送')
      countdown.value = 60
      timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(timer)
        }
      }, 1000)
    }
  } catch (error) {
    console.error('发送验证码失败:', error)
  }
}

const handleVerifyPhone = async () => {
  if (!phoneFormRef.value) return

  await phoneFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      activeStep.value = 1
    } finally {
      loading.value = false
    }
  })
}

const handleResetPassword = async () => {
  if (!passwordFormRef.value) return

  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const res = await authApi.forgotPassword({
        phone: phoneForm.phone,
        code: phoneForm.code,
        newPassword: passwordForm.newPassword
      })
      if (res.code === 0) {
        activeStep.value = 2
        ElMessage.success('密码重置成功')
      }
    } catch (error) {
      console.error('重置密码失败:', error)
    } finally {
      loading.value = false
    }
  })
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style lang="scss" scoped>
.forgot-container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.forgot-box {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.forgot-header {
  text-align: center;
  margin-bottom: 30px;

  h2 {
    font-size: 28px;
    color: #303133;
    margin-bottom: 10px;
  }

  p {
    font-size: 14px;
    color: #909399;
  }
}

.steps {
  margin-bottom: 30px;
}

.forgot-form {
  .el-form-item {
    margin-bottom: 22px;
  }
}

.code-input {
  display: flex;
  gap: 10px;

  .el-input {
    flex: 1;
  }
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
}

.success-content {
  padding: 20px 0;
}

.forgot-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;

  a {
    color: #409eff;
  }
}
</style>
