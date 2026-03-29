<template>
  <div class="user-profile">
    <div class="card">
      <div class="card-header">
        <span class="card-title">个人信息</span>
      </div>

      <el-form ref="formRef" :model="profileForm" :rules="profileRules" label-width="100px" class="profile-form">
        <el-form-item label="头像">
          <div class="avatar-upload">
            <el-avatar :size="80" :src="userStore.avatar">
              <el-icon><UserFilled /></el-icon>
            </el-avatar>
            <el-button type="primary" link @click="$router.push('/user/avatar')">修改头像</el-button>
          </div>
        </el-form-item>

        <el-form-item label="用户名">
          <el-input v-model="profileForm.username" disabled />
        </el-form-item>

        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="profileForm.nickname" placeholder="请输入昵称" />
        </el-form-item>

        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="profileForm.gender">
            <el-radio label="MALE">男</el-radio>
            <el-radio label="FEMALE">女</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="出生日期" prop="birthday">
          <el-date-picker
            v-model="profileForm.birthday"
            type="date"
            placeholder="选择出生日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="手机号">
          <el-input v-model="profileForm.phone" disabled>
            <template #append>
              <el-button @click="showPhoneDialog = true">修改</el-button>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
        </el-form-item>

        <el-form-item label="地址">
          <el-input v-model="profileForm.address" placeholder="请输入地址" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSave">保存修改</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-dialog v-model="showPhoneDialog" title="修改手机号" width="500px">
      <el-form ref="phoneFormRef" :model="phoneForm" :rules="phoneRules" label-width="100px">
        <el-form-item label="新手机号" prop="newPhone">
          <el-input v-model="phoneForm.newPhone" placeholder="请输入新手机号" />
        </el-form-item>
        <el-form-item label="验证码" prop="code">
          <div class="code-input">
            <el-input v-model="phoneForm.code" placeholder="请输入验证码" maxlength="6" />
            <el-button :disabled="countdown > 0" @click="sendCode">
              {{ countdown > 0 ? `${countdown}s后重发` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPhoneDialog = false">取消</el-button>
        <el-button type="primary" :loading="phoneLoading" @click="updatePhone">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import authApi from '@/api/auth'
import userApi from '@/api/user'
import { isValidPhone, isValidSmsCode, isValidEmail } from '@/utils/validate'

const userStore = useUserStore()
const loading = ref(false)
const phoneLoading = ref(false)
const showPhoneDialog = ref(false)
const countdown = ref(0)
const formRef = ref(null)
const phoneFormRef = ref(null)

const profileForm = reactive({
  username: '',
  nickname: '',
  gender: '',
  birthday: '',
  phone: '',
  email: '',
  address: ''
})

const phoneForm = reactive({
  newPhone: '',
  code: ''
})

const validateEmail = (rule, value, callback) => {
  if (value && !isValidEmail(value)) {
    callback(new Error('请输入正确的邮箱地址'))
  } else {
    callback()
  }
}

const profileRules = {
  nickname: [{ max: 20, message: '昵称不能超过20个字符', trigger: 'blur' }],
  email: [{ validator: validateEmail, trigger: 'blur' }]
}

const validateNewPhone = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入新手机号'))
  } else if (!isValidPhone(value)) {
    callback(new Error('请输入正确的手机号'))
  } else {
    callback()
  }
}

const validateCode = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入验证码'))
  } else if (!isValidSmsCode(value)) {
    callback(new Error('请输入6位数字验证码'))
  } else {
    callback()
  }
}

const phoneRules = {
  newPhone: [{ required: true, validator: validateNewPhone, trigger: 'blur' }],
  code: [{ required: true, validator: validateCode, trigger: 'blur' }]
}

let timer = null

const sendCode = async () => {
  if (!phoneForm.newPhone || !isValidPhone(phoneForm.newPhone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }

  try {
    const res = await authApi.sendSmsCode(phoneForm.newPhone)
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

const handleSave = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const genderValue = profileForm.gender === 'MALE' ? 1 : profileForm.gender === 'FEMALE' ? 0 : null
      const res = await authApi.updateUserInfo({
        updateData: {
          nickname: profileForm.nickname,
          gender: genderValue,
          birthday: profileForm.birthday ? new Date(profileForm.birthday).getTime() : null,
          email: profileForm.email,
          address: profileForm.address
        }
      })
      if (res.code === 0) {
        userStore.updateUserInfo({
          nickname: profileForm.nickname,
          gender: genderValue,
          birthday: profileForm.birthday ? new Date(profileForm.birthday).getTime() : null,
          email: profileForm.email,
          address: profileForm.address
        })
        ElMessage.success('保存成功')
      }
    } catch (error) {
      console.error('保存失败:', error)
    } finally {
      loading.value = false
    }
  })
}

const updatePhone = async () => {
  if (!phoneFormRef.value) return

  await phoneFormRef.value.validate(async (valid) => {
    if (!valid) return

    phoneLoading.value = true
    try {
      const res = await userApi.updatePhone({
        newPhone: phoneForm.newPhone,
        code: phoneForm.code
      })
      if (res.code === 0) {
        profileForm.phone = phoneForm.newPhone
        userStore.updateUserInfo({ phone: phoneForm.newPhone })
        ElMessage.success('手机号修改成功')
        showPhoneDialog.value = false
      }
    } catch (error) {
      console.error('修改手机号失败:', error)
    } finally {
      phoneLoading.value = false
    }
  })
}

const initForm = () => {
  const user = userStore.userInfo || {}
  profileForm.username = user.username || ''
  profileForm.nickname = user.nickname || ''
  profileForm.gender = user.gender === 1 ? 'MALE' : user.gender === 0 ? 'FEMALE' : ''
  profileForm.birthday = user.birthday ? new Date(user.birthday).toISOString().split('T')[0] : ''
  profileForm.phone = user.phone || ''
  profileForm.email = user.email || ''
  profileForm.address = user.address || ''
}

const fetchUserInfo = async () => {
  try {
    const res = await authApi.getCurrentUserInfo()
    if (res.code === 0 && res.data) {
      const user = res.data
      userStore.setUser({
        id: user.userId,
        username: user.username,
        nickname: user.nickname,
        phone: user.phone,
        email: user.email,
        gender: user.gender,
        avatar: user.avatarUrl,
        address: user.address,
        birthday: user.birthday
      })
      initForm()
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

onMounted(() => {
  fetchUserInfo()
})
</script>

<style lang="scss" scoped>
.user-profile {
  padding: 0;
}

.profile-form {
  max-width: 600px;
}

.avatar-upload {
  display: flex;
  align-items: center;
  gap: 16px;
}

.code-input {
  display: flex;
  gap: 10px;

  .el-input {
    flex: 1;
  }
}
</style>
