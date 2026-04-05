import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { title: '注册', requiresAuth: false }
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('@/views/auth/ForgotPassword.vue'),
    meta: { title: '忘记密码', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layouts/DefaultLayout.vue'),
    redirect: '/workbench',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Index.vue'),
        meta: { title: '首页', requiresAuth: true }
      },
      {
        path: 'workbench',
        name: 'Workbench',
        component: () => import('@/views/workbench/Index.vue'),
        meta: { title: '工作台', requiresAuth: true }
      }
    ]
  },
  {
    path: '/patient',
    component: () => import('@/layouts/DefaultLayout.vue'),
    redirect: '/patient/dashboard',
    meta: { roles: ['PATIENT'] },
    children: [
      {
        path: 'dashboard',
        name: 'PatientDashboard',
        component: () => import('@/views/patient/Dashboard.vue'),
        meta: { title: '患者首页', requiresAuth: true, roles: ['PATIENT'] }
      },
      {
        path: 'health-report',
        name: 'PatientHealthReport',
        component: () => import('@/views/patient/HealthReport.vue'),
        meta: { title: '健康报告', requiresAuth: true, roles: ['PATIENT'] }
      },
      {
        path: 'health-report/upload',
        name: 'UploadHealthReport',
        component: () => import('@/views/patient/UploadReport.vue'),
        meta: { title: '上传报告', requiresAuth: true, roles: ['PATIENT'] }
      },
      {
        path: 'health-report/detail/:id',
        name: 'HealthReportDetail',
        component: () => import('@/views/patient/ReportDetail.vue'),
        meta: { title: '报告详情', requiresAuth: true, roles: ['PATIENT'] }
      },
      {
        path: 'glucose-prediction',
        name: 'GlucosePrediction',
        component: () => import('@/views/patient/GlucosePrediction.vue'),
        meta: { title: '血糖预测', requiresAuth: true, roles: ['PATIENT'] }
      },
      {
        path: 'appointment',
        name: 'AppointmentDoctor',
        component: () => import('@/views/patient/AppointmentDoctor.vue'),
        meta: { title: '预约医生', requiresAuth: true, roles: ['PATIENT'] }
      }
    ]
  },
  {
    path: '/doctor',
    component: () => import('@/layouts/DefaultLayout.vue'),
    redirect: '/doctor/schedule',
    meta: { roles: ['DOCTOR'] },
    children: [
      {
        path: 'schedule',
        name: 'DoctorSchedule',
        component: () => import('@/views/doctor/Schedule.vue'),
        meta: { title: '日程管理', requiresAuth: true, roles: ['DOCTOR'] }
      },
      {
        path: 'schedule/detail/:id',
        name: 'ScheduleDetail',
        component: () => import('@/views/doctor/ScheduleDetail.vue'),
        meta: { title: '日程详情', requiresAuth: true, roles: ['DOCTOR'] }
      },
      {
        path: 'patient/:id',
        name: 'PatientInfo',
        component: () => import('@/views/doctor/PatientInfo.vue'),
        meta: { title: '患者详情', requiresAuth: true, roles: ['DOCTOR'] }
      },
      {
        path: 'leave',
        name: 'DoctorLeave',
        component: () => import('@/views/doctor/LeaveRequest.vue'),
        meta: { title: '请假申请', requiresAuth: true, roles: ['DOCTOR'] }
      },
      {
        path: 'report-approval',
        name: 'ReportApproval',
        component: () => import('@/views/doctor/ReportApproval.vue'),
        meta: { title: '报告审批', requiresAuth: true, roles: ['DOCTOR'] }
      },
      {
        path: 'report/:id',
        name: 'DoctorReportDetail',
        component: () => import('@/views/doctor/ReportDetail.vue'),
        meta: { title: '报告详情', requiresAuth: true, roles: ['DOCTOR'] }
      }
    ]
  },
  {
    path: '/admin',
    component: () => import('@/layouts/DefaultLayout.vue'),
    redirect: '/admin/workbench',
    meta: { roles: ['ADMIN'] },
    children: [
      {
        path: 'workbench',
        name: 'AdminWorkbench',
        component: () => import('@/views/admin/Workbench.vue'),
        meta: { title: '管理后台', requiresAuth: true, roles: ['ADMIN'] }
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/UserManagement.vue'),
        meta: { title: '用户管理', requiresAuth: true, roles: ['ADMIN'] }
      },
      {
        path: 'roles',
        name: 'AdminRoles',
        component: () => import('@/views/admin/RoleManagement.vue'),
        meta: { title: '角色管理', requiresAuth: true, roles: ['ADMIN'] }
      },
      {
        path: 'leave-approval',
        name: 'AdminLeaveApproval',
        component: () => import('@/views/admin/LeaveApproval.vue'),
        meta: { title: '休假审批', requiresAuth: true, roles: ['ADMIN'] }
      }
    ]
  },
  {
    path: '/message',
    component: () => import('@/layouts/DefaultLayout.vue'),
    redirect: '/message/inbox',
    children: [
      {
        path: 'inbox',
        name: 'MessageInbox',
        component: () => import('@/views/message/Inbox.vue'),
        meta: { title: '收件箱', requiresAuth: true }
      },
      {
        path: 'sent',
        name: 'MessageSent',
        component: () => import('@/views/message/Sent.vue'),
        meta: { title: '发件箱', requiresAuth: true }
      },
      {
        path: 'detail/:id',
        name: 'MessageDetail',
        component: () => import('@/views/message/Detail.vue'),
        meta: { title: '消息详情', requiresAuth: true }
      }
    ]
  },
  {
    path: '/user',
    component: () => import('@/layouts/DefaultLayout.vue'),
    redirect: '/user/profile',
    children: [
      {
        path: 'profile',
        name: 'UserProfile',
        component: () => import('@/views/user/Profile.vue'),
        meta: { title: '个人信息', requiresAuth: true }
      },
      {
        path: 'avatar',
        name: 'UserAvatar',
        component: () => import('@/views/user/Avatar.vue'),
        meta: { title: '修改头像', requiresAuth: true }
      },
      {
        path: 'password',
        name: 'UserPassword',
        component: () => import('@/views/user/Password.vue'),
        meta: { title: '修改密码', requiresAuth: true }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 慢病管理AI平台` : '慢病管理AI平台'

  const authStore = useAuthStore()
  const userStore = useUserStore()

  if (to.meta.requiresAuth === false) {
    next()
    return
  }

  if (!authStore.isAuthenticated) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  if (to.meta.roles && to.meta.roles.length > 0) {
    if (!userStore.hasAnyRole(to.meta.roles)) {
      if (userStore.hasRole('PATIENT')) {
        next({ name: 'PatientDashboard' })
      } else if (userStore.hasRole('DOCTOR')) {
        next({ name: 'DoctorSchedule' })
      } else if (userStore.hasRole('ADMIN')) {
        next({ name: 'AdminWorkbench' })
      } else {
        next({ name: 'Workbench' })
      }
      return
    }
  }

  next()
})

export default router
