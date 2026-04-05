<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h2>慢病管理AI平台</h2>
        <p>AI-driven Chronic Disease Management Platform</p>
      </div>

      <el-tabs v-model="loginType" class="login-tabs">
        <el-tab-pane label="账号密码登录" name="password">
          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            class="login-form"
          >
            <el-form-item prop="loginAccount">
              <el-input
                v-model="passwordForm.loginAccount"
                placeholder="请输入账号"
                prefix-icon="User"
                size="large"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="passwordForm.password"
                type="password"
                placeholder="请输入密码"
                prefix-icon="Lock"
                size="large"
                show-password
                @keyup.enter="handlePasswordLogin"
              />
            </el-form-item>
            <el-form-item>
              <div class="login-options">
                <el-checkbox v-model="rememberMe">记住密码</el-checkbox>
                <router-link to="/forgot-password" class="forgot-link">忘记密码？</router-link>
              </div>
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="loading"
                class="login-btn"
                @click="handlePasswordLogin"
              >
                登 录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="手机号登录" name="phone">
          <el-form
            ref="phoneFormRef"
            :model="phoneForm"
            :rules="phoneRules"
            class="login-form"
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
                  @keyup.enter="handlePhoneLogin"
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
                class="login-btn"
                @click="handlePhoneLogin"
              >
                登 录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <div class="login-footer">
        <span>还没有账号？</span>
        <router-link to="/register">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { useUserStore } from '@/stores/user'
import authApi from '@/api/auth'
import { isValidPhone, isValidSmsCode } from '@/utils/validate'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const userStore = useUserStore()

const loginType = ref('password')
const loading = ref(false)
const countdown = ref(0)
const rememberMe = ref(false)

const passwordFormRef = ref(null)
const phoneFormRef = ref(null)

const passwordForm = reactive({
  loginAccount: '',
  password: ''
})

const phoneForm = reactive({
  phone: '',
  code: ''
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

const passwordRules = {
  loginAccount: [
    { required: true, message: '请输入账号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
  ]
}

const phoneRules = {
  phone: [
    { required: true, validator: validatePhone, trigger: 'blur' }
  ],
  code: [
    { required: true, validator: validateSmsCode, trigger: 'blur' }
  ]
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

const handlePasswordLogin = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const res = await authApi.login(passwordForm)
      if (res.code === 0) {
        handleLoginSuccess(res.data)
      }
    } catch (error) {
      console.error('登录失败:', error)
    } finally {
      loading.value = false
    }
  })
}

const handlePhoneLogin = async () => {
  if (!phoneFormRef.value) return
  
  await phoneFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const res = await authApi.login({
        phone: phoneForm.phone,
        code: phoneForm.code,
        loginType: 'phone'
      })
      if (res.code === 0) {
        handleLoginSuccess(res.data)
      }
    } catch (error) {
      console.error('登录失败:', error)
    } finally {
      loading.value = false
    }
  })
}

const handleLoginSuccess = (data) => {
  console.log('Login response data:', data)
  console.log('Roles from server:', data.role)
  
  authStore.setTokens({
    accessToken: data.accessToken,
    refreshToken: data.refreshToken,
    tokenType: data.tokenType || 'Bearer'
  })

  userStore.setUser({
    id: data.userId,
    username: data.username,
    nickname: data.nickname,
    phone: data.phone,
    email: data.email,
    gender: data.gender,
    avatar: data.avatarUrl,
    address: data.address,
    birthday: data.birthday,
    roles: data.role || [],
    permissions: data.permission || []
  })
  userStore.setRoles(data.role || [])
  userStore.setPermissions(data.permission || [])

  console.log('Stored roles:', userStore.roles)
  
  ElMessage.success('登录成功')

  setTimeout(() => {
    const redirect = route.query.redirect
    if (redirect) {
      router.push(redirect)
      return
    }
    
    const roles = data.role || []
    if (roles.includes('PATIENT')) {
      router.push('/patient/dashboard')
    } else if (roles.includes('DOCTOR')) {
      router.push('/doctor/schedule')
    } else if (roles.includes('ADMIN')) {
      router.push('/admin/workbench')
    } else {
      router.push('/workbench')
    }
  }, 100)
}

onMounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style lang="scss" scoped>
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.login-header {
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

.login-tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 30px;
  }

  :deep(.el-tabs__nav-wrap::after) {
    height: 1px;
  }

  :deep(.el-tabs__item) {
    font-size: 16px;
  }
}

.login-form {
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

.login-options {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;

  .forgot-link {
    color: #409eff;
    font-size: 14px;
  }
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #909399;

  a {
    color: #409eff;
    margin-left: 5px;
  }
}
</style>
