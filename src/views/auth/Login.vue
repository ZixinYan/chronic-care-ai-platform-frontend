<template>
  <div class="login-container">
    <div class="bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
      <div class="circle circle-4"></div>
    </div>

    <div class="login-box">
      <div class="login-header">
        <div class="logo-wrapper">
          <div class="logo-icon-box">
            <PlatformIcon :size="32" />
          </div>
        </div>
        <h2>诊疗辅助系统</h2>
        <p>Medical Assistance System</p>
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
                size="large"
              >
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="passwordForm.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                show-password
                @keyup.enter="handlePasswordLogin"
              >
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
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
                size="large"
              >
                <template #prefix>
                  <el-icon><Phone /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item prop="code">
              <div class="code-input">
                <el-input
                  v-model="phoneForm.code"
                  placeholder="请输入验证码"
                  size="large"
                  maxlength="6"
                  @keyup.enter="handlePhoneLogin"
                >
                  <template #prefix>
                    <el-icon><Message /></el-icon>
                  </template>
                </el-input>
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
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Phone, Message } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { useUserStore } from '@/stores/user'
import authApi from '@/api/auth'
import { isValidPhone, isValidSmsCode } from '@/utils/validate'
import PlatformIcon from '@/components/common/PlatformIcon.vue'

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
      } else {
        ElMessage.error(res.msg || '登录失败')
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
      const res = await authApi.loginWithPhone({
        phone: phoneForm.phone,
        code: phoneForm.code,
        loginType: 'phone'
      })
      if (res.code === 0) {
        handleLoginSuccess(res.data)
      } else {
        ElMessage.error(res.msg || '登录失败')
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
      router.push('/patient/health-report')
    } else if (roles.includes('DOCTOR')) {
      router.push('/doctor/schedule')
    } else if (roles.includes('ADMIN')) {
      router.push('/admin/users')
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
  background: linear-gradient(135deg, #0c1445 0%, #1a237e 25%, #0d47a1 50%, #01579b 75%, #006064 100%);
  position: relative;
  overflow: hidden;
}

.bg-decoration {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;

  .circle {
    position: absolute;
    border-radius: 50%;
    opacity: 0.15;
  }

  .circle-1 {
    width: 600px;
    height: 600px;
    background: radial-gradient(circle, #4096ff, transparent 70%);
    top: -200px;
    right: -100px;
    animation: float1 8s ease-in-out infinite;
  }

  .circle-2 {
    width: 400px;
    height: 400px;
    background: radial-gradient(circle, #722ed1, transparent 70%);
    bottom: -100px;
    left: -100px;
    animation: float2 10s ease-in-out infinite;
  }

  .circle-3 {
    width: 300px;
    height: 300px;
    background: radial-gradient(circle, #13c2c2, transparent 70%);
    top: 50%;
    left: 20%;
    animation: float3 12s ease-in-out infinite;
  }

  .circle-4 {
    width: 200px;
    height: 200px;
    background: radial-gradient(circle, #eb2f96, transparent 70%);
    bottom: 20%;
    right: 15%;
    animation: float1 9s ease-in-out infinite reverse;
  }
}

@keyframes float1 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(-30px, 20px) scale(1.05); }
}

@keyframes float2 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(20px, -30px) scale(1.08); }
}

@keyframes float3 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(-20px, -20px) scale(1.03); }
}

.login-box {
  width: 440px;
  padding: 44px 40px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 20px;
  box-shadow:
    0 25px 80px rgba(0, 0, 0, 0.3),
    0 0 0 1px rgba(255, 255, 255, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.5);
  position: relative;
  z-index: 1;
  animation: boxAppear 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes boxAppear {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.login-header {
  text-align: center;
  margin-bottom: 32px;

  .logo-wrapper {
    margin-bottom: 16px;
  }

  .logo-icon-box {
    width: 56px;
    height: 56px;
    border-radius: 16px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #4096ff 0%, #1677ff 50%, #0958d9 100%);
    box-shadow: 0 8px 24px rgba(64, 150, 255, 0.35);
    color: #fff;
    position: relative;

    &::before {
      content: '';
      position: absolute;
      inset: -2px;
      border-radius: 18px;
      background: linear-gradient(135deg, rgba(255,255,255,0.4), transparent);
      z-index: -1;
    }
  }

  h2 {
    font-size: 24px;
    font-weight: 700;
    color: #1a1a2e;
    margin-bottom: 8px;
    letter-spacing: 2px;
    font-family: 'Inter', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  }

  p {
    font-size: 13px;
    color: #8c8c8c;
    letter-spacing: 0.8px;
    font-family: 'Inter', sans-serif;
    font-weight: 400;
  }
}

.login-tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 28px;
  }

  :deep(.el-tabs__nav-wrap::after) {
    height: 1px;
    background: #f0f0f0;
  }

  :deep(.el-tabs__item) {
    font-size: 15px;
    font-weight: 500;
    padding: 0 4px;
    transition: color 0.3s;

    &.is-active {
      font-weight: 600;
    }
  }

  :deep(.el-tabs__active-bar) {
    height: 3px;
    border-radius: 2px;
    background: linear-gradient(90deg, #4096ff, #1677ff);
  }
}

.login-form {
  .el-form-item {
    margin-bottom: 22px;
  }

  :deep(.el-input__wrapper) {
    border-radius: 10px;
    padding: 4px 12px;
    box-shadow: 0 0 0 1px #e8e8e8 inset;
    transition: all 0.3s;

    &:hover {
      box-shadow: 0 0 0 1px #4096ff inset;
    }

    &.is-focus {
      box-shadow: 0 0 0 1px #4096ff inset, 0 0 0 3px rgba(64, 150, 255, 0.1);
    }
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
    color: #4096ff;
    font-size: 14px;
    transition: color 0.3s;

    &:hover {
      color: #1677ff;
    }
  }
}

.login-btn {
  width: 100%;
  height: 46px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 4px;
  border-radius: 10px;
  background: linear-gradient(135deg, #4096ff 0%, #1677ff 100%);
  border: none;
  box-shadow: 0 4px 16px rgba(64, 150, 255, 0.35);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 6px 20px rgba(64, 150, 255, 0.45);
    background: linear-gradient(135deg, #69b1ff 0%, #4096ff 100%);
  }

  &:active {
    transform: translateY(0);
    box-shadow: 0 2px 8px rgba(64, 150, 255, 0.3);
  }
}

.login-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #8c8c8c;

  a {
    color: #4096ff;
    margin-left: 4px;
    font-weight: 500;
    transition: color 0.3s;

    &:hover {
      color: #1677ff;
    }
  }
}
</style>
