<script setup>
import {Lock, User} from "@element-plus/icons-vue";
import {reactive, ref} from "vue";
import {ElMessage} from "element-plus";
import router from "@/router/index.js";
import WelcomeFooter from "@/components/WelcomeFooter.vue";
import {login} from "@/net/index.js";

const form =reactive({
  username:'',
  password:'',
  remember:false
})
const formRef = ref()
const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'));
  } else if (/\s/.test(value)) {  // Checks for spaces, tabs, or newline characters
    callback(new Error('密码不得包含空格、制表符或换行符'));
  } else {
    callback();
  }
}
function userLogin(){
  formRef.value.validate(valid => {
    if (valid){
      login(form.username,form.password,form.remember,()=>{
        router.push('/index')
      })
    }
  })
}

const rules = {
  username: [
    // {validator: validateUsername, trigger: ['blur', 'change']},
    {required: true, message: '请输入用户名/账号', trigger: 'blur'},
    {min: 3, max: 16, message: '用户名长度在 3 到 16 个字符', trigger: ['blur']}
  ],
  password: [
    {required: true, message: '请输入密码', trigger: 'blur'},
    {min: 6, max: 16, message: '长度在 6 到 16 个字符之间', trigger: ['blur', 'change']},
    {validator: validatePassword, trigger: ['blur', 'change']}
  ]
}

</script>

<template>
  <div style="text-align: center;margin: 0 20px;">

    <!--        //头-->
    <div style="margin-top: 150px">
      <div style="font-size: 25px;font-weight: bold">登录</div>
      <div style="font-size: 14px;color: gray">请输入用户名和密码</div>
    </div>

    <!--        //表单-->
    <div style="margin-top: 50px">
      <el-form :model="form" :rules="rules" ref="formRef">

        <el-form-item prop="username">
          <el-input v-model="form.username" type="text" placeholder="用户名/邮箱" maxlength="10">
            <template #prefix>
              <el-icon>
                <User/>
              </el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" maxlength="20">
            <template #prefix>
              <el-icon>
                <Lock/>
              </el-icon>
            </template>
          </el-input>
        </el-form-item>

      </el-form>
    </div>

    <el-row style="margin-top: 5px">
      <el-col :span="12" style="text-align: left;">
        <el-checkbox v-model="form.remember" label="记住我"/>
      </el-col>
      <el-col :span="12" style="text-align: right">
        <el-link @click="router.push('forget')">忘记密码?</el-link>
      </el-col>
    </el-row>

    <div style="margin-top: 40px">
      <el-button @click="userLogin" style="width: 270px" type="success" plain>立即登录</el-button>
    </div>
    <el-divider>
      <span style="color: gray;font-size: 11px;">没有账号？</span>
    </el-divider>
    <div>
      <el-button style="width: 270px" @click="router.push('/register')" type="warning" plain>注册账号</el-button>
    </div>
    <div style="padding-top: 102px">
      <WelcomeFooter/>
    </div>
  </div>
</template>

<style scoped>


</style>