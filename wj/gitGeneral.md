# git cli
- local
1. 初始化仓库
  `git init`
2. 创建分支
  `git branch 分支名`
3. 切换分支
  `git checkout 分支名`
4. 添加文件到暂存区
  `git add 文件名`
  `git add .`
5. 合并分支
  `git merge 分支名`
// . 表示添加所有修改。
6. 提交到本地仓库
  `git commit -m "提交说明"`


- remote
1.连接到远程仓库
  `git remote add origin 仓库地址`
2.拉取远程仓库
  `git pull origin main`  //or git pull
3.推送到远程仓库
  `git push -u origin main` // or git push

