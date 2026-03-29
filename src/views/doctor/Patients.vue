<template>
  <div class="patients-manage">
    <div class="card">
      <div class="card-header">
        <span class="card-title">患者管理</span>
      </div>

      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="患者姓名">
          <el-input v-model="queryParams.name" placeholder="请输入患者姓名" clearable />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="queryParams.phone" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="patientList" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">
            {{ row.gender === 'MALE' ? '男' : '女' }}
          </template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="80" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="diagnosis" label="诊断" min-width="150" />
        <el-table-column prop="lastVisitTime" label="最近就诊" width="180" />
        <el-table-column prop="healthStatus" label="健康状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getHealthStatusTag(row.healthStatus)">{{ row.healthStatusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewPatient(row)">详情</el-button>
            <el-button type="success" link size="small" @click="createSchedule(row)">预约</el-button>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import doctorApi from '@/api/doctor'

const router = useRouter()
const loading = ref(false)
const total = ref(0)

const queryParams = reactive({
  name: '',
  phone: '',
  pageNum: 1,
  pageSize: 10
})

const patientList = ref([])

const getHealthStatusTag = (status) => {
  const tags = { GOOD: 'success', NORMAL: 'warning', POOR: 'danger' }
  return tags[status] || 'info'
}

const fetchPatientList = async () => {
  loading.value = true
  try {
    const res = await doctorApi.getMyPatients(queryParams)
    if (res.code === 0) {
      patientList.value = res.data?.list || []
      total.value = res.data?.total || 0
    }
  } catch (error) {
    console.error('获取患者列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.pageNum = 1
  fetchPatientList()
}

const handleReset = () => {
  queryParams.name = ''
  queryParams.phone = ''
  queryParams.pageNum = 1
  fetchPatientList()
}

const handleSizeChange = () => {
  fetchPatientList()
}

const handleCurrentChange = () => {
  fetchPatientList()
}

const viewPatient = (row) => {
  router.push(`/doctor/patient/${row.id}`)
}

const createSchedule = (row) => {
  router.push({ path: '/doctor/schedule', query: { patientId: row.id } })
}

onMounted(() => {
  fetchPatientList()
})
</script>

<style lang="scss" scoped>
.patients-manage {
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
