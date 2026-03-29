<template>
  <div class="user-management">
    <div class="card">
      <div class="card-header">
        <span class="card-title">用户管理</span>
      </div>

      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="关键词">
          <el-input 
            v-model="queryParams.keyword" 
            placeholder="用户名/昵称/手机号/邮箱" 
            clearable 
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="queryParams.roleCode" placeholder="全部角色" clearable style="width: 120px">
            <el-option
              v-for="role in roleOptions"
              :key="role.code"
              :label="role.name"
              :value="role.code"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="userList" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="userId" label="用户ID" width="180" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="nickname" label="昵称" width="100" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">
            {{ row.gender === 1 ? '男' : row.gender === 2 ? '女' : '未知' }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="roles" label="角色" min-width="150">
          <template #default="{ row }">
            <el-tag 
              v-for="role in row.roles" 
              :key="role" 
              :type="getRoleTagType(role)"
              style="margin-right: 4px"
            >
              {{ getRoleName(role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEditRoles(row)">分配角色</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        class="pagination"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <el-dialog v-model="roleDialogVisible" title="分配角色" width="400px">
      <el-form :model="roleForm" label-width="80px">
        <el-form-item label="用户">
          <el-input :value="currentUser?.username" disabled />
        </el-form-item>
        <el-form-item label="角色">
          <el-checkbox-group v-model="roleForm.roleCodes">
            <el-checkbox 
              v-for="role in roleOptions" 
              :key="role.code" 
              :label="role.code"
            >
              {{ role.name }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveRoles" :loading="saveLoading">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import adminApi from '@/api/admin'

const loading = ref(false)
const saveLoading = ref(false)
const total = ref(0)
const userList = ref([])
const roleOptions = ref([])
const roleDialogVisible = ref(false)
const currentUser = ref(null)

const queryParams = reactive({
  keyword: '',
  roleCode: null,
  pageNum: 1,
  pageSize: 10
})

const roleForm = reactive({
  roleCodes: []
})

const roleMap = {
  'DOCTOR': '医生',
  'PATIENT': '患者',
  'FAMILY': '家属',
  'ADMIN': '管理员'
}

const getRoleName = (role) => {
  return roleMap[role] || role
}

const getRoleTagType = (role) => {
  const types = {
    'DOCTOR': 'success',
    'PATIENT': 'primary',
    'FAMILY': 'warning',
    'ADMIN': 'danger'
  }
  return types[role] || 'info'
}

const formatTime = (timestamp) => {
  if (!timestamp) return '-'
  const date = new Date(timestamp)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const fetchUserList = async () => {
  loading.value = true
  try {
    const res = await adminApi.getUsersList(queryParams)
    if (res.code === 0) {
      userList.value = res.data?.users || []
      total.value = res.data?.total || 0
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchRoles = async () => {
  try {
    const res = await adminApi.getAllRoles()
    if (res.code === 0) {
      roleOptions.value = res.data || []
    }
  } catch (error) {
    console.error('获取角色列表失败:', error)
  }
}

const handleSearch = () => {
  queryParams.pageNum = 1
  fetchUserList()
}

const handleReset = () => {
  queryParams.keyword = ''
  queryParams.roleCode = null
  queryParams.pageNum = 1
  fetchUserList()
}

const handleSizeChange = () => {
  fetchUserList()
}

const handleCurrentChange = () => {
  fetchUserList()
}

const handleEditRoles = (row) => {
  currentUser.value = row
  roleForm.roleCodes = row.roleCodes || []
  roleDialogVisible.value = true
}

const handleSaveRoles = async () => {
  if (!currentUser.value) return
  
  saveLoading.value = true
  try {
    const res = await adminApi.updateUserRoles(currentUser.value.userId, roleForm.roleCodes)
    if (res.code === 0) {
      ElMessage.success('角色分配成功')
      roleDialogVisible.value = false
      fetchUserList()
    }
  } catch (error) {
    console.error('分配角色失败:', error)
    ElMessage.error('分配角色失败')
  } finally {
    saveLoading.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除用户 "${row.username}" 吗？此操作不可恢复。`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await adminApi.deleteUsers([row.userId])
      if (res.code === 0) {
        ElMessage.success('删除成功')
        fetchUserList()
      }
    } catch (error) {
      console.error('删除用户失败:', error)
      ElMessage.error('删除用户失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchUserList()
  fetchRoles()
})
</script>

<style lang="scss" scoped>
.user-management {
  padding: 0;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>
