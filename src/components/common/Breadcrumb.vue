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
  padding: 16px 20px;
  background-color: #fff;
  border-bottom: 1px solid #ebeef5;
}

.no-redirect {
  color: #97a8be;
  cursor: text;
}

a {
  color: #606266;
  cursor: pointer;

  &:hover {
    color: #409eff;
  }
}
</style>
