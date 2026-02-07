<template>
  <div id="app">
    <router-view />
  </div>
</template>

<style>
:root {
  --app-font-family: "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", "Noto Sans CJK SC",
    "Source Han Sans SC", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue",
    Arial, sans-serif;
  --el-font-family: var(--app-font-family);
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: var(--app-font-family);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  line-height: 1.5;
}

button,
input,
select,
textarea {
  font-family: inherit;
}

#app {
  min-height: 100vh;
}

/* 全局按钮与图标对齐，避免文字/图标基线偏移 */
.el-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.el-button > span {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  line-height: 1.2;
}

.el-button .el-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  vertical-align: middle;
  margin: 0;
}

.el-button .el-icon svg {
  display: block;
}

/* 输入框附加按钮统一垂直对齐 */
.el-input-group__append .el-button,
.el-input-group__prepend .el-button {
  height: 100%;
}

/* 表单控件文字垂直对齐微调 */
.el-input__wrapper,
.el-select .el-input__wrapper,
.el-date-editor .el-input__wrapper {
  align-items: center;
}

.el-input__inner {
  line-height: 1.3;
}

.el-textarea__inner {
  line-height: 1.6;
}

.el-form-item__label {
  display: inline-flex;
  align-items: center;
  line-height: 1.35;
}

.el-dropdown-menu__item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.el-checkbox__label,
.el-radio__label {
  line-height: 1.35;
}
</style>
