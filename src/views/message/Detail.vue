<template>
  <div class="message-detail">
    <div class="card">
      <div class="card-header">
        <span class="card-title">消息详情</span>
        <el-button @click="$router.back()">返回</el-button>
      </div>

      <div class="message-content">
        <h2 class="message-title">{{ messageDetail.title }}</h2>
        <div class="message-meta">
          <span class="meta-item">
            <el-icon><User /></el-icon>
            发送者：{{ messageDetail.senderName }}
          </span>
          <span class="meta-item" v-if="messageDetail.recipientNames && messageDetail.recipientNames.length > 0">
            <el-icon><User /></el-icon>
            <span v-if="messageDetail.recipientNames.length > 1">
              收件人：
              <el-tooltip placement="top">
                <template #content>
                  <div v-for="name in messageDetail.recipientNames" :key="name">{{ name }}</div>
                </template>
                <span class="recipient-names">{{ messageDetail.recipientNames.slice(0, 3).join('、') }}<span v-if="messageDetail.recipientNames.length > 3">等{{ messageDetail.recipientNames.length }}人</span></span>
              </el-tooltip>
            </span>
            <span v-else>收件人：{{ messageDetail.recipientNames[0] }}</span>
          </span>
          <span class="meta-item" v-else-if="messageDetail.receiverName">
            <el-icon><User /></el-icon>
            收件人：{{ messageDetail.receiverName }}
          </span>
          <span class="meta-item">
            <el-icon><Clock /></el-icon>
            发送时间：{{ messageDetail.sendTime }}
          </span>
        </div>
        <el-divider />
        <div class="message-body" v-html="messageDetail.content"></div>
      </div>

      <div v-if="messageDetail.attachments && messageDetail.attachments.length > 0" class="attachments mt-20">
        <h4>附件</h4>
        <div class="attachment-list">
          <div v-for="file in messageDetail.attachments" :key="file.id" class="attachment-item">
            <el-icon><Document /></el-icon>
            <span>{{ file.name }}</span>
            <el-button type="primary" link size="small" @click="downloadAttachment(file)">下载</el-button>
          </div>
        </div>
      </div>

      <div class="actions mt-20">
        <el-button v-if="isReceiver" type="primary" @click="replyMessage">回复</el-button>
        <el-button type="danger" @click="deleteMessage">删除</el-button>
      </div>
    </div>

    <el-dialog v-model="replyDialogVisible" title="回复消息" width="600px">
      <el-form ref="replyFormRef" :model="replyForm" :rules="replyRules" label-width="80px">
        <el-form-item label="内容" prop="content">
          <el-input v-model="replyForm.content" type="textarea" :rows="5" placeholder="请输入回复内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="replyLoading" @click="submitReply">发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Clock, Document } from '@element-plus/icons-vue'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import messageApi from '@/api/message'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()
const replyDialogVisible = ref(false)
const replyLoading = ref(false)
const replyFormRef = ref(null)

const messageDetail = ref({
  id: null,
  title: '',
  content: '',
  senderName: '',
  senderId: null,
  receiverId: null,
  receiverName: '',
  recipientNames: [],
  sendTime: '',
  isRead: false,
  attachments: []
})

const isReceiver = ref(false)

const replyForm = reactive({
  content: ''
})

const replyRules = {
  content: [{ required: true, message: '请输入回复内容', trigger: 'blur' }]
}

const fetchMessageDetail = async () => {
  const messageId = route.params.id
  if (!messageId) return

  try {
    const res = await messageApi.getDetail(messageId)
    if (res.code === 0) {
      const data = res.data || {}
      messageDetail.value = {
        id: data.messageId,
        title: data.title,
        content: data.content,
        senderName: data.senderName,
        senderId: data.senderId,
        receiverId: data.receiverId,
        receiverName: data.receiverName,
        recipientNames: data.recipientNames || [],
        sendTime: data.createTime ? new Date(data.createTime).toLocaleString() : '',
        isRead: data.status === 1,
        attachments: []
      }
      
      const currentUserId = userStore.userId
      isReceiver.value = currentUserId && String(currentUserId) === String(data.receiverId)
      
      if (data.status !== 1 && isReceiver.value) {
        messageApi.markAsRead(messageId)
        appStore.decrementUnreadCount()
      }
    }
  } catch (error) {
    console.error('获取消息详情失败:', error)
  }
}

const replyMessage = () => {
  replyDialogVisible.value = true
}

const submitReply = async () => {
  if (!replyFormRef.value) return

  await replyFormRef.value.validate(async (valid) => {
    if (!valid) return

    replyLoading.value = true
    try {
      const res = await messageApi.sendEmail(
        replyForm.content,
        messageDetail.value.senderId,
        `Re: ${messageDetail.value.title}`
      )
      if (res.code === 0) {
        ElMessage.success('回复成功')
        replyDialogVisible.value = false
      }
    } catch (error) {
      console.error('回复失败:', error)
    } finally {
      replyLoading.value = false
    }
  })
}

const deleteMessage = async () => {
  try {
    await ElMessageBox.confirm('确定要删除该消息吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await messageApi.deleteMessage(messageDetail.value.id)
    if (res.code === 0) {
      ElMessage.success('删除成功')
      router.back()
    }
  } catch {
    // 用户取消
  }
}

const downloadAttachment = (file) => {
  if (file.url) {
    window.open(file.url, '_blank')
  }
}

onMounted(() => {
  fetchMessageDetail()
})
</script>

<style lang="scss" scoped>
.message-detail {
  padding: 0;
}

.message-content {
  padding: 20px 0;
}

.message-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
}

.message-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
  color: #909399;
  font-size: 14px;

  .meta-item {
    display: flex;
    align-items: center;
    gap: 6px;
  }
}

.recipient-names {
  cursor: pointer;
  color: #409eff;
}

.message-body {
  font-size: 14px;
  color: #606266;
  line-height: 1.8;
}

.attachments {
  h4 {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 12px;
    color: #303133;
  }
}

.attachment-list {
  .attachment-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 12px;
    background-color: #f5f7fa;
    border-radius: 4px;
    margin-bottom: 8px;

    .el-icon {
      font-size: 20px;
      color: #409eff;
    }

    span {
      flex: 1;
      font-size: 14px;
      color: #606266;
    }
  }
}

.actions {
  display: flex;
  gap: 12px;
}
</style>
