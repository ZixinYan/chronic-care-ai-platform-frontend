<template>
  <div class="message-inbox">
    <div class="card">
      <div class="card-header">
        <span class="card-title">收件箱</span>
        <div class="header-actions">
          <el-button type="primary" :disabled="selectedIds.length === 0" @click="batchMarkAsRead">
            标记已读
          </el-button>
          <el-button type="danger" :disabled="selectedIds.length === 0" @click="batchDelete">
            删除
          </el-button>
        </div>
      </div>

      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="是否已读">
          <el-select v-model="queryParams.isRead" placeholder="全部" clearable>
            <el-option label="未读" :value="false" />
            <el-option label="已读" :value="true" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table
        :data="messageList"
        stripe
        v-loading="loading"
        @selection-change="handleSelectionChange"
        style="width: 100%"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-badge v-if="!row.isRead" is-dot class="unread-badge">未读</el-badge>
            <span v-else class="read-text">已读</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="200">
          <template #default="{ row }">
            <span :class="{ 'unread-title': !row.isRead }">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="senderName" label="发送者" width="120" />
        <el-table-column prop="sendTime" label="发送时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewMessage(row)">查看</el-button>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAppStore } from '@/stores/app'
import messageApi from '@/api/message'

const router = useRouter()
const appStore = useAppStore()
const loading = ref(false)
const total = ref(0)
const selectedIds = ref([])

const queryParams = reactive({
  isRead: null,
  pageNum: 1,
  pageSize: 10
})

const messageList = ref([])

const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

const fetchMessageList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize
    }
    if (queryParams.isRead === false) {
      params.unreadOnly = true
    }
    const res = await messageApi.getInbox(params)
    if (res.code === 0) {
      const list = res.data?.list || []
      messageList.value = list.map(item => ({
        id: item.messageId,
        title: item.title,
        senderName: item.senderName,
        sendTime: item.createTime ? new Date(item.createTime).toLocaleString() : '',
        isRead: item.status === 1
      }))
      total.value = res.data?.totalCount || 0
    }
  } catch (error) {
    console.error('获取消息列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.pageNum = 1
  fetchMessageList()
}

const handleReset = () => {
  queryParams.isRead = null
  queryParams.pageNum = 1
  fetchMessageList()
}

const handleSizeChange = () => {
  fetchMessageList()
}

const handleCurrentChange = () => {
  fetchMessageList()
}

const viewMessage = (row) => {
  router.push(`/message/detail/${row.id}`)
}

const batchMarkAsRead = async () => {
  try {
    const res = await messageApi.batchMarkAsRead(selectedIds.value)
    if (res.code === 0) {
      ElMessage.success('已标记为已读')
      appStore.setUnreadMessageCount(appStore.unreadMessageCount - selectedIds.value.length)
      fetchMessageList()
    }
  } catch (error) {
    console.error('标记已读失败:', error)
  }
}

const batchDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除选中的消息吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await messageApi.batchDelete(selectedIds.value)
    if (res.code === 0) {
      ElMessage.success('删除成功')
      fetchMessageList()
    }
  } catch {
    // 用户取消
  }
}

onMounted(() => {
  fetchMessageList()
})
</script>

<style lang="scss" scoped>
.message-inbox {
  padding: 0;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}

.unread-badge {
  :deep(.el-badge__content) {
    right: calc(100% + 5px);
  }
}

.read-text {
  color: #909399;
  font-size: 12px;
}

.unread-title {
  font-weight: 600;
  color: #303133;
}
</style>
