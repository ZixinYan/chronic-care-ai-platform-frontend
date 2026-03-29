<template>
  <div class="message-sent">
    <div class="card">
      <div class="card-header">
        <span class="card-title">发件箱</span>
      </div>

      <el-table :data="messageList" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="receiverName" label="接收者" width="180">
          <template #default="{ row }">
            <span v-if="row.recipientNames && row.recipientNames.length > 1">
              <el-tooltip placement="top">
                <template #content>
                  <div v-for="name in row.recipientNames" :key="name">{{ name }}</div>
                </template>
                <span class="recipient-names">{{ row.recipientNames.slice(0, 2).join('、') }}<span v-if="row.recipientNames.length > 2">等{{ row.recipientNames.length }}人</span></span>
              </el-tooltip>
            </span>
            <span v-else-if="row.recipientNames && row.recipientNames.length === 1">{{ row.recipientNames[0] }}</span>
            <span v-else>{{ row.receiverName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sendTime" label="发送时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'SENT' ? 'success' : 'warning'">
              {{ row.status === 'SENT' ? '已发送' : '发送中' }}
            </el-tag>
          </template>
        </el-table-column>
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
import messageApi from '@/api/message'

const router = useRouter()
const loading = ref(false)
const total = ref(0)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10
})

const messageList = ref([])

const fetchMessageList = async () => {
  loading.value = true
  try {
    const res = await messageApi.getSent({
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize
    })
    if (res.code === 0) {
      const list = res.data?.list || []
      messageList.value = list.map(item => ({
        id: item.messageId,
        title: item.title,
        receiverName: item.receiverName,
        recipientNames: item.recipientNames,
        sendTime: item.createTime ? new Date(item.createTime).toLocaleString() : '',
        status: item.status === 1 ? 'SENT' : 'SENDING'
      }))
      total.value = res.data?.totalCount || 0
    }
  } catch (error) {
    console.error('获取消息列表失败:', error)
  } finally {
    loading.value = false
  }
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

onMounted(() => {
  fetchMessageList()
})
</script>

<style lang="scss" scoped>
.message-sent {
  padding: 0;
}

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}

.recipient-names {
  cursor: pointer;
  color: #409eff;
}
</style>
