<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <h2>用户注册</h2>
        <p>创建您的慢病管理AI平台账号</p>
      </div>

      <el-form
        ref="formRef"
        :model="registerForm"
        :rules="registerRules"
        class="register-form"
        label-position="top"
      >
        <el-form-item prop="username" label="用户名">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名（4-16位字母数字）"
            prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="password" label="密码">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码（6-20位）"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item prop="confirmPassword" label="确认密码">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item prop="phone" label="手机号">
          <el-input
            v-model="registerForm.phone"
            placeholder="请输入手机号"
            prefix-icon="Phone"
          />
        </el-form-item>

        <el-form-item prop="code" label="验证码">
          <div class="code-input">
            <el-input
              v-model="registerForm.code"
              placeholder="请输入验证码"
              prefix-icon="Message"
              maxlength="6"
            />
            <el-button
              :disabled="countdown > 0"
              @click="handleSendCode"
            >
              {{ countdown > 0 ? `${countdown}s后重发` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>

        <el-form-item prop="realName" label="真实姓名">
          <el-input
            v-model="registerForm.realName"
            placeholder="请输入真实姓名"
            prefix-icon="UserFilled"
          />
        </el-form-item>

        <el-form-item prop="idCard" label="身份证号">
          <el-input
            v-model="registerForm.idCard"
            placeholder="请输入身份证号"
            prefix-icon="Postcard"
          />
        </el-form-item>

        <el-form-item prop="role" label="用户角色">
          <el-radio-group v-model="registerForm.role" @change="handleRoleChange">
            <el-radio label="PATIENT">患者</el-radio>
            <el-radio label="DOCTOR">医生</el-radio>
          </el-radio-group>
        </el-form-item>

        <template v-if="registerForm.role === 'DOCTOR'">
          <div class="role-section-title">医生信息</div>
          
          <el-form-item prop="doctor.department" label="科室">
            <el-select v-model="registerForm.doctor.department" placeholder="请选择科室" style="width: 100%">
              <el-option label="内科" value="内科" />
              <el-option label="内分泌科" value="内分泌科" />
              <el-option label="心血管科" value="心血管科" />
              <el-option label="神经内科" value="神经内科" />
              <el-option label="全科" value="全科" />
            </el-select>
          </el-form-item>

          <el-form-item prop="doctor.title" label="职称">
            <el-select v-model="registerForm.doctor.title" placeholder="请选择职称" style="width: 100%">
              <el-option label="住院医师" value="住院医师" />
              <el-option label="主治医师" value="主治医师" />
              <el-option label="副主任医师" value="副主任医师" />
              <el-option label="主任医师" value="主任医师" />
            </el-select>
          </el-form-item>

          <el-form-item prop="doctor.experience" label="工作经验（年）">
            <el-input-number v-model="registerForm.doctor.experience" :min="0" :max="50" style="width: 100%" />
          </el-form-item>

          <el-form-item prop="doctor.education" label="学历">
            <el-select v-model="registerForm.doctor.education" placeholder="请选择学历" style="width: 100%">
              <el-option label="大专" value="大专" />
              <el-option label="本科" value="本科" />
              <el-option label="硕士" value="硕士" />
              <el-option label="博士" value="博士" />
            </el-select>
          </el-form-item>

          <el-form-item prop="doctor.bio" label="个人简介">
            <el-input
              v-model="registerForm.doctor.bio"
              type="textarea"
              :rows="3"
              placeholder="请输入个人简介"
            />
          </el-form-item>
        </template>

        <template v-if="registerForm.role === 'PATIENT'">
          <div class="role-section-title">患者信息</div>
          
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item prop="patient.bloodType" label="血型">
                <el-select v-model="registerForm.patient.bloodType" placeholder="请选择" style="width: 100%">
                  <el-option label="A型" value="A" />
                  <el-option label="B型" value="B" />
                  <el-option label="AB型" value="AB" />
                  <el-option label="O型" value="O" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item prop="patient.height" label="身高(cm)">
                <el-input-number v-model="registerForm.patient.height" :min="50" :max="250" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item prop="patient.weight" label="体重(kg)">
                <el-input-number v-model="registerForm.patient.weight" :min="20" :max="300" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item prop="patient.medicalHistory" label="病史摘要">
            <el-input
              v-model="registerForm.patient.medicalHistory"
              type="textarea"
              :rows="2"
              placeholder="请输入病史摘要（可选）"
            />
          </el-form-item>

          <el-form-item prop="patient.allergies" label="过敏史">
            <el-input
              v-model="registerForm.patient.allergies"
              placeholder="请输入过敏史（可选）"
            />
          </el-form-item>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item prop="patient.emergencyContact" label="紧急联系人">
                <el-input
                  v-model="registerForm.patient.emergencyContact"
                  placeholder="请输入紧急联系人姓名"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item prop="patient.emergencyPhone" label="紧急联系人电话">
                <el-input
                  v-model="registerForm.patient.emergencyPhone"
                  placeholder="请输入紧急联系人电话"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </template>

        <el-form-item prop="agreement">
          <el-checkbox v-model="registerForm.agreement">
            我已阅读并同意
            <a href="javascript:;" @click.stop="showAgreement">《用户协议》</a>
            和
            <a href="javascript:;" @click.stop="showPrivacy">《隐私政策》</a>
          </el-checkbox>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="register-btn"
            @click="handleRegister"
          >
            注 册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="register-footer">
        <span>已有账号？</span>
        <router-link to="/login">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import authApi from '@/api/auth'
import { isValidPhone, isValidUsername, isValidPassword, isValidRealName, isValidIdCard, isValidSmsCode } from '@/utils/validate'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const countdown = ref(0)

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  phone: '',
  code: '',
  realName: '',
  idCard: '',
  role: 'PATIENT',
  agreement: false,
  doctor: {
    department: '',
    title: '',
    experience: 0,
    education: '',
    bio: ''
  },
  patient: {
    bloodType: '',
    height: null,
    weight: null,
    medicalHistory: '',
    allergies: '',
    emergencyContact: '',
    emergencyPhone: ''
  }
})

const validateUsername = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入用户名'))
  } else if (!isValidUsername(value)) {
    callback(new Error('用户名为4-16位字母、数字或下划线'))
  } else {
    callback()
  }
}

const validatePassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入密码'))
  } else if (!isValidPassword(value)) {
    callback(new Error('密码长度为6-20位'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

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

const validateRealName = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入真实姓名'))
  } else if (!isValidRealName(value)) {
    callback(new Error('请输入2-10位中文姓名'))
  } else {
    callback()
  }
}

const validateIdCard = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入身份证号'))
  } else if (!isValidIdCard(value)) {
    callback(new Error('请输入正确的身份证号'))
  } else {
    callback()
  }
}

const validateAgreement = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请阅读并同意用户协议和隐私政策'))
  } else {
    callback()
  }
}

const registerRules = {
  username: [{ required: true, validator: validateUsername, trigger: 'blur' }],
  password: [{ required: true, validator: validatePassword, trigger: 'blur' }],
  confirmPassword: [{ required: true, validator: validateConfirmPassword, trigger: 'blur' }],
  phone: [{ required: true, validator: validatePhone, trigger: 'blur' }],
  code: [{ required: true, validator: validateSmsCode, trigger: 'blur' }],
  realName: [{ required: true, validator: validateRealName, trigger: 'blur' }],
  idCard: [{ required: true, validator: validateIdCard, trigger: 'blur' }],
  role: [{ required: true, message: '请选择用户角色', trigger: 'change' }],
  agreement: [{ required: true, validator: validateAgreement, trigger: 'change' }]
}

let timer = null

const handleRoleChange = () => {
  registerForm.doctor = {
    department: '',
    title: '',
    experience: 0,
    education: '',
    bio: ''
  }
  registerForm.patient = {
    bloodType: '',
    height: null,
    weight: null,
    medicalHistory: '',
    allergies: '',
    emergencyContact: '',
    emergencyPhone: ''
  }
}

const handleSendCode = async () => {
  if (!registerForm.phone || !isValidPhone(registerForm.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }

  try {
    const res = await authApi.sendSmsCode(registerForm.phone)
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

const handleRegister = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const roleCodeMap = {
        'DOCTOR': 1,
        'PATIENT': 2
      }
      
      const requestData = {
        username: registerForm.username,
        password: registerForm.password,
        phone: registerForm.phone,
        code: registerForm.code,
        nickname: registerForm.realName,
        idCard: registerForm.idCard,
        roleCodes: [roleCodeMap[registerForm.role]]
      }

      if (registerForm.role === 'DOCTOR') {
        if (!registerForm.doctor.department) {
          ElMessage.warning('请选择科室')
          loading.value = false
          return
        }
        if (!registerForm.doctor.title) {
          ElMessage.warning('请选择职称')
          loading.value = false
          return
        }
        requestData.doctor = {
          department: registerForm.doctor.department,
          title: registerForm.doctor.title,
          experience: registerForm.doctor.experience || 0,
          education: registerForm.doctor.education || '',
          bio: registerForm.doctor.bio || ''
        }
      }

      if (registerForm.role === 'PATIENT') {
        requestData.patient = {
          bloodType: registerForm.patient.bloodType || '',
          height: registerForm.patient.height || null,
          weight: registerForm.patient.weight || null,
          medicalHistory: registerForm.patient.medicalHistory || '',
          allergies: registerForm.patient.allergies || '',
          emergencyContact: registerForm.patient.emergencyContact || '',
          emergencyPhone: registerForm.patient.emergencyPhone || ''
        }
      }

      const res = await authApi.register(requestData)
      if (res.code === 0) {
        ElMessage.success('注册成功，请登录')
        router.push('/login')
      }
    } catch (error) {
      console.error('注册失败:', error)
    } finally {
      loading.value = false
    }
  })
}

const showAgreement = () => {
  ElMessage.info('用户协议内容')
}

const showPrivacy = () => {
  ElMessage.info('隐私政策内容')
}
</script>

<style lang="scss" scoped>
.register-container {
  width: 100%;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-box {
  width: 520px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  max-height: 90vh;
  overflow-y: auto;
}

.register-header {
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

.register-form {
  .el-form-item {
    margin-bottom: 18px;
  }

  :deep(.el-form-item__label) {
    font-weight: 500;
    padding-bottom: 8px;
  }
}

.code-input {
  display: flex;
  gap: 10px;

  .el-input {
    flex: 1;
  }
}

.role-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #409eff;
  margin: 20px 0 15px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.register-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  margin-top: 10px;
}

.register-footer {
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
