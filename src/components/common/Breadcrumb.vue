<template>
  <div class="breadcrumb-container">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
        <span v-if="item.redirect === 'noRedirect' || item === breadcrumbs[breadcrumbs.length - 1]" class="no-redirect">
          {{ item.meta?.title || item.name }}
        </span>
        <a v-else @click.prevent="handleLink(item)">{{ item.meta?.title || item.name }}</a>
      </el-breadcrumb-item>
    </el-breadcrumb>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const breadcrumbs = ref([])

const getBreadcrumbs = () => {
  const matched = route.matched.filter(item => item.meta && item.meta.title)
  const first = matched[0]

  if (!first || first.path !== '/dashboard') {
    matched.unshift({
      path: '/dashboard',
      meta: { title: '首页' }
    })
  }

  breadcrumbs.value = matched
}

const handleLink = (item) => {
  const { path, redirect } = item
  if (redirect) {
    router.push(redirect)
    return
  }
  router.push(path)
}

watch(
  () => route.path,
  () => {
    getBreadcrumbs()
  },
  { immediate: true }
)
</script>

<style lang="scss" scoped>
.breadcrumb-container {
  padding: 14px 24px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.03);
}

.no-redirect {
  color: #8c8c8c;
  cursor: text;
  font-weight: 500;
}

a {
  color: #595959;
  cursor: pointer;
  font-weight: 400;
  transition: color 0.3s;

  &:hover {
    color: #4096ff;
  }
}

:deep(.el-breadcrumb__separator) {
  color: #d9d9d9;
}
</style>
