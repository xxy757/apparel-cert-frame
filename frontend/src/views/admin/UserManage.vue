<template>
  <div class="user-manage-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <h2>用户管理</h2>
          <el-button type="primary" @click="openCreateUserDialog">添加用户</el-button>
        </div>
      </template>
      
      <div class="user-filters">
        <el-form :inline="true" :model="searchForm" label-width="80px">
          <el-form-item label="用户名">
            <el-input v-model="searchForm.username" placeholder="请输入用户名"></el-input>
          </el-form-item>
          <el-form-item label="用户类型">
            <el-select v-model="searchForm.userType" placeholder="请选择用户类型">
              <el-option label="全部" value=""></el-option>
              <el-option label="个人用户" value="personal"></el-option>
              <el-option label="企业用户" value="enterprise"></el-option>
              <el-option label="管理员" value="admin"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="用户状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态">
              <el-option label="全部" value=""></el-option>
              <el-option label="正常" value="1"></el-option>
              <el-option label="禁用" value="0"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchUsers">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <el-table :data="users" style="width: 100%">
        <el-table-column prop="id" label="用户ID" width="100"></el-table-column>
        <el-table-column prop="username" label="用户名"></el-table-column>
        <el-table-column prop="email" label="邮箱"></el-table-column>
        <el-table-column prop="phone" label="电话" width="120"></el-table-column>
        <el-table-column prop="userType" label="用户类型" width="120">
          <template #default="scope">
            <el-tag :type="getUserTypeTagType(scope.row.userType)">
              {{ getUserTypeText(scope.row.userType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180"></el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewUserDetails(scope.row)">查看详情</el-button>
            <el-button type="warning" size="small" @click="editUser(scope.row)">编辑</el-button>
            <el-button v-if="scope.row.status === 1" type="danger" size="small" @click="disableUser(scope.row)">禁用</el-button>
            <el-button v-else type="success" size="small" @click="enableUser(scope.row)">启用</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalUsers"
        ></el-pagination>
      </div>
    </el-card>
    
    <!-- 添加用户对话框 -->
    <el-dialog v-model="createUserDialogVisible" title="添加用户" width="600px">
      <el-form ref="userFormRef" :model="userForm" :rules="userRules" label-width="120px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱"></el-input>
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入电话"></el-input>
        </el-form-item>
        <el-form-item label="用户类型" prop="userType">
          <el-select v-model="userForm.userType" placeholder="请选择用户类型">
            <el-option label="个人用户" value="personal"></el-option>
            <el-option label="企业用户" value="enterprise"></el-option>
            <el-option label="管理员" value="admin"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="用户状态" prop="status">
          <el-select v-model="userForm.status" placeholder="请选择用户状态">
            <el-option label="正常" value="1"></el-option>
            <el-option label="禁用" value="0"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createUserDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="createUser">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

export default {
  name: 'UserManage',
  setup() {
    const userFormRef = ref(null)
    const createUserDialogVisible = ref(false)
    const searchForm = reactive({
      username: '',
      userType: '',
      status: ''
    })
    const userForm = reactive({
      username: '',
      password: '',
      email: '',
      phone: '',
      userType: 'personal',
      status: '1'
    })
    const userRules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
      ],
      email: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
      ],
      phone: [
        { required: true, message: '请输入电话', trigger: 'blur' },
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
      ],
      userType: [
        { required: true, message: '请选择用户类型', trigger: 'change' }
      ],
      status: [
        { required: true, message: '请选择用户状态', trigger: 'change' }
      ]
    }
    const users = ref([])
    const currentPage = ref(1)
    const pageSize = ref(10)
    const totalUsers = ref(0)
    
    // 模拟用户数据
    const mockUsers = [
      { id: 1, username: '张三', email: 'zhangsan@example.com', phone: '13800138001', userType: 'personal', status: 1, createTime: '2024-01-15 10:30:00' },
      { id: 2, username: '李四', email: 'lisi@example.com', phone: '13800138002', userType: 'enterprise', status: 1, createTime: '2024-01-16 14:20:00' },
      { id: 3, username: '王五', email: 'wangwu@example.com', phone: '13800138003', userType: 'personal', status: 1, createTime: '2024-01-17 09:15:00' },
      { id: 4, username: '赵六', email: 'zhaoliu@example.com', phone: '13800138004', userType: 'admin', status: 1, createTime: '2024-01-18 16:45:00' },
      { id: 5, username: '孙七', email: 'sunqi@example.com', phone: '13800138005', userType: 'enterprise', status: 0, createTime: '2024-01-19 11:20:00' },
      { id: 6, username: '周八', email: 'zhouba@example.com', phone: '13800138006', userType: 'personal', status: 1, createTime: '2024-01-20 15:30:00' },
      { id: 7, username: '吴九', email: 'wujiu@example.com', phone: '13800138007', userType: 'personal', status: 1, createTime: '2024-01-21 08:45:00' },
      { id: 8, username: '郑十', email: 'zhengshi@example.com', phone: '13800138008', userType: 'enterprise', status: 1, createTime: '2024-01-22 13:10:00' }
    ]
    
    onMounted(() => {
      loadUsers()
    })
    
    const loadUsers = () => {
      // 模拟加载用户数据
      users.value = mockUsers
      totalUsers.value = mockUsers.length
    }
    
    const searchUsers = () => {
      // 模拟搜索功能
      let filteredUsers = [...mockUsers]
      if (searchForm.username) {
        filteredUsers = filteredUsers.filter(user => user.username.includes(searchForm.username))
      }
      if (searchForm.userType) {
        filteredUsers = filteredUsers.filter(user => user.userType === searchForm.userType)
      }
      if (searchForm.status) {
        filteredUsers = filteredUsers.filter(user => user.status === parseInt(searchForm.status))
      }
      users.value = filteredUsers
      totalUsers.value = filteredUsers.length
      currentPage.value = 1
    }
    
    const resetSearch = () => {
      searchForm.username = ''
      searchForm.userType = ''
      searchForm.status = ''
      loadUsers()
    }
    
    const getUserTypeText = (type) => {
      const typeMap = {
        personal: '个人用户',
        enterprise: '企业用户',
        admin: '管理员'
      }
      return typeMap[type] || '未知'
    }
    
    const getUserTypeTagType = (type) => {
      const typeMap = {
        personal: 'info',
        enterprise: 'success',
        admin: 'warning'
      }
      return typeMap[type] || 'info'
    }
    
    const getStatusText = (status) => {
      return status === 1 ? '正常' : '禁用'
    }
    
    const getStatusTagType = (status) => {
      return status === 1 ? 'success' : 'danger'
    }
    
    const openCreateUserDialog = () => {
      // 重置表单
      userForm.username = ''
      userForm.password = ''
      userForm.email = ''
      userForm.phone = ''
      userForm.userType = 'personal'
      userForm.status = '1'
      createUserDialogVisible.value = true
    }
    
    const createUser = () => {
      userFormRef.value.validate((valid) => {
        if (valid) {
          // 模拟添加用户
          ElMessage.success('用户添加成功')
          createUserDialogVisible.value = false
          loadUsers()
        } else {
          return false
        }
      })
    }
    
    const viewUserDetails = (user) => {
      // 查看用户详情逻辑
      ElMessage.info('查看用户详情功能开发中')
    }
    
    const editUser = (user) => {
      // 编辑用户逻辑
      ElMessage.info('编辑用户功能开发中')
    }
    
    const disableUser = (user) => {
      // 禁用用户逻辑
      ElMessage.success('用户已禁用')
      user.status = 0
    }
    
    const enableUser = (user) => {
      // 启用用户逻辑
      ElMessage.success('用户已启用')
      user.status = 1
    }
    
    const handleSizeChange = (size) => {
      pageSize.value = size
      loadUsers()
    }
    
    const handleCurrentChange = (current) => {
      currentPage.value = current
      loadUsers()
    }
    
    return {
      userFormRef,
      createUserDialogVisible,
      searchForm,
      userForm,
      userRules,
      users,
      currentPage,
      pageSize,
      totalUsers,
      getUserTypeText,
      getUserTypeTagType,
      getStatusText,
      getStatusTagType,
      openCreateUserDialog,
      createUser,
      searchUsers,
      resetSearch,
      viewUserDetails,
      editUser,
      disableUser,
      enableUser,
      handleSizeChange,
      handleCurrentChange
    }
  }
}
</script>

<style scoped>
.user-manage-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 20px;
  color: #333;
}

.user-filters {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>