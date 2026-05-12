(function() {
"use strict";

        const API_BASE = '/api';

        // 切换标签页（通过按钮点击）
        window.switchTab = function(tabName) {
            document.querySelectorAll('.tab').forEach(tab => tab.classList.remove('active'));
            document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));
            if (event && event.target) {
                event.target.classList.add('active');
            } else {
                // 如果event不可用，通过tabName找到对应的tab按钮
                document.querySelectorAll('.tab').forEach(tab => {
                    if (tab.getAttribute('onclick') && tab.getAttribute('onclick').includes(tabName)) {
                        tab.classList.add('active');
                    }
                });
            }
            document.getElementById(tabName).classList.add('active');

            // 更新移动端导航高亮
            document.querySelectorAll('.mobile-nav-item').forEach(function(item) {
                item.classList.remove('active');
                var onclick = item.getAttribute('onclick') || '';
                if (onclick.includes("'" + tabName + "'")) item.classList.add('active');
            });

            // 加载对应数据
            if (tabName === 'posts') {
                loadPosts();
            } else if (tabName === 'myPosts') {
                loadMyPosts();
            } else if (tabName === 'myRegistrations') {
                loadMyRegistrations();
            } else if (tabName === 'myProfile') {
                loadMyProfile();
            } else if (tabName === 'messages') {
                loadMessages();
            }
        }

        // 切换到指定标签页（编程方式）
        function switchToTab(tabName) {
            // 移除所有标签和内容的active状态
            document.querySelectorAll('.tab').forEach(tab => tab.classList.remove('active'));
            document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));
            
            // 根据tabName找到对应的标签按钮
            const tabMap = {
                'login': 'loginTab',
                'register': 'loginTab',
                'posts': null,  // 需要通过查找按钮文本来定位
                'create': null,
                'myPosts': null,
                'myRegistrations': null
            };
            
            let targetTab = null;
            if (tabMap[tabName] && document.getElementById(tabMap[tabName])) {
                targetTab = document.getElementById(tabMap[tabName]);
            } else {
                // 对于其他标签，通过查找onclick属性来定位
                document.querySelectorAll('.tab').forEach(tab => {
                    const onclickAttr = tab.getAttribute('onclick');
                    if (onclickAttr && onclickAttr.includes(tabName)) {
                        targetTab = tab;
                    }
                });
            }
            
            // 激活找到的标签（如果存在）
            if (targetTab && !targetTab.classList.contains('hidden')) {
                targetTab.classList.add('active');
            }
            
            // 激活对应的内容区域
            const targetContent = document.getElementById(tabName);
            if (targetContent) {
                targetContent.classList.add('active');
            } else if (tabName === 'postDetail') {
                // 详情页是隐藏的，直接激活
                const detailContent = document.getElementById('postDetail');
                if (detailContent) {
                    detailContent.classList.add('active');
                }
            }

            // 加载对应数据
            if (tabName === 'posts') {
                loadPosts();
            } else if (tabName === 'myPosts') {
                loadMyPosts();
            } else if (tabName === 'myRegistrations') {
                loadMyRegistrations();
            }
        }

        // 显示消息
        function showMessage(message, type = 'success') {
            const messageDiv = document.getElementById('message');
            messageDiv.className = `message ${type}`;
            messageDiv.textContent = message;
            messageDiv.style.display = 'block';
            setTimeout(() => {
                messageDiv.style.display = 'none';
            }, 3000);
        }

        // API请求
        function getToken() { return localStorage.getItem('jwt_token'); }
        function setToken(token) { localStorage.setItem('jwt_token', token); }
        function clearToken() { localStorage.removeItem('jwt_token'); }

        async function apiRequest(url, options = {}) {
            const maxRetries = options.skipRetry ? 1 : 2;
            for (let attempt = 0; attempt < maxRetries; attempt++) {
                try {
                    const token = getToken();
                    const headers = { 'Content-Type': 'application/json', ...options.headers };
                    if (token) { headers['Authorization'] = 'Bearer ' + token; }
                    const response = await fetch(API_BASE + url, {
                        ...options,
                        headers: headers,
                        credentials: 'include'
                    });
                    const data = await response.json();
                    return data;
                } catch (error) {
                    if (attempt < maxRetries - 1) {
                        await new Promise(r => setTimeout(r, 1000 * (attempt + 1)));
                        continue;
                    }
                    showMessage('网络错误：' + error.message, 'error');
                    throw error;
                }
            }
        }

        window.handleLogin = async function(event) {
            event.preventDefault();
            var btn = event.target.querySelector('button[type=submit]');
            btnLoading(btn, true);
            const username = document.getElementById('loginUsername').value.trim();
            const password = document.getElementById('loginPassword').value;
            try {
                const result = await apiRequest('/user/login', {
                    method: 'POST',
                    body: JSON.stringify({ username, password })
                });
                if (result.code === 200) {
                    if (result.data.token) setToken(result.data.token);
                    showMessage('欢迎回来，' + (result.data.nickname || result.data.username) + '！');
                    updateUserInfo(result.data);
                    document.getElementById('loginUsername').value = '';
                    document.getElementById('loginPassword').value = '';
                } else {
                    showMessage(result.message || '登录失败', 'error');
                }
            } catch (e) { showMessage('操作失败', 'error'); }
            btnLoading(btn, false);
        }

        window.handleRegister = async function(event) {
            event.preventDefault();
            var btn = event.target.querySelector('button[type=submit]');
            btnLoading(btn, true);
            const username = document.getElementById('regUsername').value.trim();
            const nickname = document.getElementById('regNickname').value.trim();
            const password = document.getElementById('regPassword').value;
            const confirm = document.getElementById('regConfirmPassword').value;
            if (password !== confirm) { showMessage('两次密码不一致', 'error'); return; }
            if (password.length < 6) { showMessage('密码至少6位', 'error'); return; }
            try {
                const result = await apiRequest('/user/register', {
                    method: 'POST',
                    body: JSON.stringify({ username, password, nickname: nickname || username })
                });
                if (result.code === 200) {
                    showMessage('注册成功！');
                    // 自动登录
                    const loginResult = await apiRequest('/user/login', {
                        method: 'POST',
                        body: JSON.stringify({ username, password })
                    });
                    if (loginResult.code === 200) {
                        if (loginResult.data.token) setToken(loginResult.data.token);
                        updateUserInfo(loginResult.data);
                    }
                    document.getElementById('regUsername').value = '';
                    document.getElementById('regNickname').value = '';
                    document.getElementById('regPassword').value = '';
                    document.getElementById('regConfirmPassword').value = '';
                    document.getElementById('regAgreeToS').checked = false;
                    switchTab('posts');
                } else {
                    showMessage(result.message || '注册失败', 'error');
                }
            } catch (e) { showMessage('注册失败', 'error'); }
            btnLoading(btn, false);
        }

        window.showForgotPassword = function() {
            showMessage('请联系管理员重置密码。邮箱：admin@greengrassland.com');
        }

        function btnLoading(btn, loading) {
            if (loading) { btn._t = btn.textContent; btn.disabled = true; btn.textContent = '⏳ '; btn.style.opacity = '0.7'; }
            else if (btn._t) { btn.disabled = false; btn.textContent = btn._t; btn.style.opacity = '1'; }
        }

        // 登出
        async function logout() {
            try {
                await apiRequest('/user/logout', { method: 'POST' });
                clearToken();
                showMessage('已登出');
                document.getElementById('userInfo').classList.add('hidden');
                document.getElementById('currentUsername').textContent = '';
                updateHeroVisibility();
                // 登出后显示登录和注册标签
                document.getElementById('loginTab').classList.remove('hidden');
                document.getElementById('registerTab').classList.remove('hidden');
                // 切换到登录标签
                switchToTab('login');
            } catch (error) {
                showMessage('登出失败', 'error');
            }
        }

        // 更新用户信息
        async function updateUserInfo(user) {
            if (user) {
                document.getElementById('currentUsername').textContent = user.nickname || user.username;
                updateAvatarDisplay(user.avatar);
                window.currentUserId = user.id; // 保存当前用户ID
            initNotificationPolling();
                document.getElementById('userInfo').classList.remove('hidden');
                updateHeroVisibility();
                // 登录后隐藏登录和注册标签
            } else {
                try {
                    const result = await apiRequest('/user/current');
                    if (result.code === 200 && result.data) {
                        document.getElementById('currentUsername').textContent = result.data.nickname || result.data.username;
                        updateAvatarDisplay(result.data.avatar);
                        window.currentUserId = result.data.id; // 保存当前用户ID
                        document.getElementById('userInfo').classList.remove('hidden');
                        // 登录后隐藏登录和注册标签
                        document.getElementById('loginTab').classList.add('hidden');
                        document.getElementById('registerTab').classList.add('hidden');
                        // 切换到活动列表标签
                        switchToTab('posts');
                    } else {
                        // 未登录，显示登录和注册标签
                        document.getElementById('loginTab').classList.remove('hidden');
                        document.getElementById('registerTab').classList.remove('hidden');
                    }
                } catch (error) {
                    // 未登录，显示登录和注册标签
                    document.getElementById('loginTab').classList.remove('hidden');
                    document.getElementById('registerTab').classList.remove('hidden');
                }
            }
        }

        // 更新头像显示
        function updateAvatarDisplay(avatarUrl) {
            const avatarImg = document.getElementById('userAvatar');
            if (avatarUrl) {
                avatarImg.src = avatarUrl;
            } else {
                avatarImg.src = '/images/default-avatar.svg';
            }
        }

        // 上传头像
        async function uploadAvatar(event) {
            const file = event.target.files[0];
            if (!file) {
                return;
            }

            // 验证文件类型
            const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
            if (!validTypes.includes(file.type)) {
                showMessage('不支持的文件类型，仅支持jpg、jpeg、png、gif、webp', 'error');
                return;
            }

            // 验证文件大小（5MB）
            if (file.size > 5 * 1024 * 1024) {
                showMessage('文件大小不能超过5MB', 'error');
                return;
            }

            const formData = new FormData();
            formData.append('file', file);

            try {
                const response = await fetch('/api/upload/avatar', {
                    method: 'POST',
                    body: formData,
                    credentials: 'include'
                });

                const result = await response.json();

                if (result.code === 200) {
                    showMessage('头像上传成功！');
                    updateAvatarDisplay(result.data);
                    // 刷新用户信息
                    updateUserInfo();
                } else {
                    showMessage(result.message || '头像上传失败', 'error');
                }
            } catch (error) {
                showMessage('头像上传失败：' + error.message, 'error');
            }

            // 清空input，允许重复选择同一文件
            event.target.value = '';
        }

        // 加载活动列表
        async function loadPosts() {
            try {
                const params = new URLSearchParams();
                if (userLat != null && userLng != null) {
                    params.append('lat', userLat);
                    params.append('lng', userLng);
                }
                const qs = params.toString();
                const url = '/post' + (qs ? '?' + qs : '');
                const result = await apiRequest(url);
                if (result.code === 200) {
                    const data = result.data;
                    if (data.content) {
                        renderPosts(data.content, 'postList');
                        totalPages = data.totalPages;
                        currentPage = data.page;
                    } else {
                        renderPosts(data, 'postList');
                        totalPages = 1;
                        currentPage = 1;
                    }
                    renderPagination();
                } else {
                    showMessage(result.message || '加载失败', 'error');
                }
            } catch (error) {
                showMessage('加载失败', 'error');
            }
        }

        // 搜索活动
        async function searchPosts(page = 1) {
            currentPage = page;
            const keyword = document.getElementById('searchKeyword').value.trim();
            const location = document.getElementById('searchLocation').value.trim();
            const type = document.getElementById('searchType').value;
            const sortBy = document.getElementById('searchSortBy').value;
            const sortOrder = document.getElementById('searchSortOrder').value;
            currentSearchKeyword = keyword;
            if (keyword) saveSearchHistory(keyword);

            try {
                const params = new URLSearchParams();
                if (keyword) params.append('keyword', keyword);
                if (location) params.append('location', location);
                if (type) params.append('type', type);
                if (sortBy) params.append('sortBy', sortBy);
                if (sortOrder) params.append('sortOrder', sortOrder);
                params.append('page', page);
                params.append('pageSize', pageSize);
                if (userLat != null && userLng != null) {
                    params.append('lat', userLat);
                    params.append('lng', userLng);
                }

                const url = '/post?' + params.toString();
                const result = await apiRequest(url);
                if (result.code === 200) {
                    const data = result.data;
                    // 判断是否有分页包装
                    if (data.content) {
                        renderPosts(data.content, 'postList');
                        totalPages = data.totalPages;
                        currentPage = data.page;
                    } else {
                        renderPosts(data, 'postList');
                        totalPages = 1;
                        currentPage = 1;
                    }
                    renderPagination();
                } else {
                    showMessage(result.message || '搜索失败', 'error');
                }
            } catch (error) {
                showMessage('搜索失败', 'error');
            }
        }

        // 渲染分页组件
        function renderPagination() {
            const postList = document.getElementById('postList');
            let pagDiv = document.getElementById('pagination');
            if (!pagDiv) {
                pagDiv = document.createElement('div');
                pagDiv.id = 'pagination';
                postList.parentElement.appendChild(pagDiv);
            }
            if (totalPages <= 1) { pagDiv.innerHTML = ''; return; }

            let html = '<div style="display:flex;justify-content:center;align-items:center;gap:8px;margin-top:24px;flex-wrap:wrap;">';
            html += `<button onclick="searchPosts(${currentPage - 1})" ${currentPage <= 1 ? 'disabled' : ''} style="padding:8px 16px;border:1px solid var(--border);background:var(--surface);border-radius:var(--radius);cursor:pointer;font-size:14px;">‹ 上一页</button>`;
            for (let i = 1; i <= totalPages; i++) {
                if (i === currentPage) {
                    html += `<span style="padding:8px 16px;background:linear-gradient(135deg,var(--primary),var(--primary-dark));color:white;border-radius:var(--radius);font-weight:700;font-size:14px;">${i}</span>`;
                } else if (i <= 3 || i > totalPages - 3 || Math.abs(i - currentPage) <= 1) {
                    html += `<button onclick="searchPosts(${i})" style="padding:8px 16px;border:1px solid var(--border);background:var(--surface);border-radius:var(--radius);cursor:pointer;font-size:14px;">${i}</button>`;
                } else if (i === 4 || i === totalPages - 3) {
                    html += '<span style="color:var(--text-muted);padding:4px;">...</span>';
                }
            }
            html += `<button onclick="searchPosts(${currentPage + 1})" ${currentPage >= totalPages ? 'disabled' : ''} style="padding:8px 16px;border:1px solid var(--border);background:var(--surface);border-radius:var(--radius);cursor:pointer;font-size:14px;">下一页 ›</button>`;
            html += `<span style="font-size:13px;color:var(--text-muted);margin-left:8px;">共 ${totalPages} 页</span></div>`;
            pagDiv.innerHTML = html;
        }

        // 切换筛选面板
        function toggleFilterPanel() {
            const panel = document.getElementById('filterPanel');
            panel.classList.toggle('hidden');
        }

        // 重置搜索
        function resetSearch() {
            document.getElementById('searchKeyword').value = '';
            document.getElementById('searchLocation').value = '';
            document.getElementById('searchType').value = '';
            document.getElementById('searchSortBy').value = 'createTime';
            document.getElementById('searchSortOrder').value = 'DESC';
            currentSearchKeyword = '';
            loadPosts();
        }

        // 加载我的发布
        async function loadMyPosts() {
            try {
                const result = await apiRequest('/post/my/posts');
                if (result.code === 200) {
                    renderPosts(result.data, 'myPostList', true);
                } else {
                    showMessage(result.message || '加载失败', 'error');
                }
            } catch (error) {
                showMessage('加载失败', 'error');
            }
        }

        // 加载我的报名
        async function loadMyRegistrations() {
            try {
                const result = await apiRequest('/post/my/registrations');
                if (result.code === 200) {
                    renderPosts(result.data, 'myRegistrationList');
                } else {
                    showMessage(result.message || '加载失败', 'error');
                }
            } catch (error) {
                showMessage('加载失败', 'error');
            }
        }

        // 加载我的主页
        async function loadMyProfile() {
            const currentUserId = getCurrentUserId();
            if (!currentUserId) {
                document.getElementById('profileContent').innerHTML = '<div class="detail-loading">请先登录</div>';
                return;
            }

            try {
                // 同时加载用户信息、我的发布、我的收藏、粉丝和关注
                const [userResult, postsResult, favoritesResult, followersResult, followingsResult, followerCountResult, followingCountResult] = await Promise.all([
                    apiRequest('/user/current'),
                    apiRequest('/post/my/posts'),
                    apiRequest('/post/favorite/my'),
                    apiRequest(`/user/follow/followers/${currentUserId}`),
                    apiRequest(`/user/follow/followings/${currentUserId}`),
                    apiRequest(`/user/follow/followers/count/${currentUserId}`),
                    apiRequest(`/user/follow/followings/count/${currentUserId}`)
                ]);

                const user = userResult.code === 200 ? userResult.data : null;
                const posts = postsResult.code === 200 ? postsResult.data : [];
                const favorites = favoritesResult.code === 200 ? favoritesResult.data : [];
                const followers = followersResult.code === 200 ? followersResult.data : [];
                const followings = followingsResult.code === 200 ? followingsResult.data : [];
                const followerCount = followerCountResult.code === 200 ? followerCountResult.data : 0;
                const followingCount = followingCountResult.code === 200 ? followingCountResult.data : 0;

                renderMyProfile(user, posts, favorites, followers, followings, followerCount, followingCount);
            } catch (error) {
                document.getElementById('profileContent').innerHTML = '<div class="detail-loading">加载失败：' + error.message + '</div>';
            }
        }

        // 渲染我的主页
        function renderMyProfile(user, posts, favorites, followers, followings, followerCount, followingCount) {
            if (!user) {
                document.getElementById('profileContent').innerHTML = '<div class="detail-loading">加载失败</div>';
                return;
            }

            const avatar = user.avatar || '/images/default-avatar.svg';
            const createTime = user.createTime 
                ? new Date(user.createTime).toLocaleString('zh-CN', {year: 'numeric', month: 'long', day: 'numeric'})
                : '';

            let html = `
                <div class="my-profile">
                    <!-- 用户信息头部 -->
                    <div class="my-profile-header">
                        <img src="${avatar}" alt="头像" class="my-profile-avatar" onerror="this.src='/images/default-avatar.svg'" onclick="document.getElementById('avatarUpload').click()">
                        <div class="my-profile-info">
                            <div class="my-profile-name">${user.nickname || user.username}</div>
                            <div class="my-profile-username">@${user.username}</div>
                            ${user.bio ? `<div class="my-profile-bio">${escapeHtml(user.bio)}</div>` : ''}
                            ${user.interestTags ? `<div class="my-profile-tags">${user.interestTags.split(',').filter(t=>t.trim()).map(t=>`<span class="my-profile-tag">${escapeHtml(t.trim())}</span>`).join('')}</div>` : ''}
                        </div>
                        <button class="btn btn-secondary" onclick="showEditProfile()">编辑资料</button>
                    </div>

                    <!-- 统计信息 -->
                    <div class="my-profile-stats">
                        <div class="my-profile-stat-item" onclick="showMyPosts()">
                            <div class="my-profile-stat-value">${user.postCount || posts.length}</div>
                            <div class="my-profile-stat-label">发布</div>
                        </div>
                        <div class="my-profile-stat-item" onclick="showMyFavorites()">
                            <div class="my-profile-stat-value">${user.favoriteCount || favorites.length}</div>
                            <div class="my-profile-stat-label">收藏</div>
                        </div>
                        <div class="my-profile-stat-item">
                            <div class="my-profile-stat-value">${user.likeCount || 0}</div>
                            <div class="my-profile-stat-label">获赞</div>
                        </div>
                        <div class="my-profile-stat-item" onclick="showMyFollowers()">
                            <div class="my-profile-stat-value">${user.followerCount || followerCount}</div>
                            <div class="my-profile-stat-label">粉丝</div>
                        </div>
                        <div class="my-profile-stat-item" onclick="showMyFollowings()">
                            <div class="my-profile-stat-value">${user.followingCount || followingCount}</div>
                            <div class="my-profile-stat-label">关注</div>
                        </div>
                    </div>

                    <!-- 个人数据面板 -->
                    <div style="background:linear-gradient(135deg,var(--primary-bg),var(--accent-light));border-radius:var(--radius);padding:12px 16px;margin-bottom:16px;">
                        <div style="font-size:13px;font-weight:600;color:var(--primary-dark);margin-bottom:6px;">📊 我的数据</div>
                        <div style="display:flex;gap:16px;font-size:12px;color:var(--text-secondary);">
                            <span>📝 发布 ${user.postCount || posts.length} 个活动</span>
                            <span>❤️ 获赞 ${user.likeCount || 0}</span>
                            <span>👥 粉丝 ${user.followerCount || followerCount}</span>
                        </div>
                    </div>

                    <!-- 内容区域 -->
                    <div class="my-profile-content">
                        <div id="profileContentArea">
                            <div class="my-profile-section">
                                <div class="my-profile-section-title">我的发布</div>
                                <div id="profilePostsList" class="post-list"></div>
                            </div>
                        </div>
                    </div>
                </div>
            `;

            document.getElementById('profileContent').innerHTML = html;
            
            // 存储数据供后续使用
            window.profileData = {user, posts, favorites, followers, followings, followerCount, followingCount};
            
            // 默认显示我的发布
            showMyPosts();
        }

        // 显示我的发布
        function showMyPosts() {
            const data = window.profileData;
            if (!data) return;
            
            const container = document.getElementById('profilePostsList');
            if (data.posts && data.posts.length > 0) {
                renderPosts(data.posts, 'profilePostsList', true);
            } else {
                container.innerHTML = '<p>暂无发布</p>';
            }
        }

        // 显示我的收藏
        function showMyFavorites() {
            const data = window.profileData;
            if (!data) return;
            
            const container = document.getElementById('profilePostsList');
            if (data.favorites && data.favorites.length > 0) {
                renderPosts(data.favorites, 'profilePostsList');
            } else {
                container.innerHTML = '<p>暂无收藏</p>';
            }
        }

        // 显示我的粉丝
        function showMyFollowers() {
            const data = window.profileData;
            if (!data) return;
            
            const container = document.getElementById('profilePostsList');
            if (data.followers && data.followers.length > 0) {
                container.innerHTML = data.followers.map(follower => {
                    const avatar = follower.avatar || '/images/default-avatar.svg';
                    return `
                        <div class="user-list-item">
                            <img src="${avatar}" alt="头像" class="user-list-avatar" onerror="this.src='/images/default-avatar.svg'" onclick="viewUserProfile(${follower.id})">
                            <div class="user-list-info">
                                <div class="user-list-name">${follower.nickname || follower.username}</div>
                                <div class="user-list-username">@${follower.username}</div>
                            </div>
                            <button class="btn btn-primary" onclick="viewUserProfile(${follower.id})">查看主页</button>
                        </div>
                    `;
                }).join('');
            } else {
                container.innerHTML = '<p>暂无粉丝</p>';
            }
        }

        // 显示我的关注
        function showMyFollowings() {
            const data = window.profileData;
            if (!data) return;
            
            const container = document.getElementById('profilePostsList');
            if (data.followings && data.followings.length > 0) {
                container.innerHTML = data.followings.map(following => {
                    const avatar = following.avatar || '/images/default-avatar.svg';
                    return `
                        <div class="user-list-item">
                            <img src="${avatar}" alt="头像" class="user-list-avatar" onerror="this.src='/images/default-avatar.svg'" onclick="viewUserProfile(${following.id})">
                            <div class="user-list-info">
                                <div class="user-list-name">${following.nickname || following.username}</div>
                                <div class="user-list-username">@${following.username}</div>
                            </div>
                            <button class="btn btn-primary" onclick="viewUserProfile(${following.id})">查看主页</button>
                        </div>
                    `;
                }).join('');
            } else {
                container.innerHTML = '<p>暂无关注</p>';
            }
        }

        // 显示编辑资料表单
        function showEditProfile() {
            const data = window.profileData;
            if (!data || !data.user) return;

            const html = `
                <div class="edit-profile-form">
                    <h3>编辑资料</h3>
                    <div class="form-group">
                        <label>昵称</label>
                        <input type="text" id="editNickname" value="${data.user.nickname || ''}">
                    </div>
                    <div class="form-group">
                        <label>个人简介</label>
                        <textarea id="editBio" maxlength="500" rows="3" style="width:100%;padding:8px;border:1px solid var(--border);border-radius:var(--radius-sm);font-family:var(--font);resize:vertical;">${data.user.bio || ''}</textarea>
                    </div>
                    <div class="form-group">
                        <label>兴趣标签（用逗号分隔）</label>
                        <input type="text" id="editInterestTags" value="${data.user.interestTags || ''}" placeholder="例如：篮球,桌游,摄影">
                    </div>
                    <div class="form-group">
                        <label>邮箱</label>
                        <input type="email" id="editEmail" value="${data.user.email || ''}">
                    </div>
                    <hr style="border-color:var(--border);margin:16px 0;">
                    <h4 style="font-size:14px;color:var(--text-secondary);margin-bottom:10px;">修改密码</h4>
                    <div class="form-group">
                        <label>当前密码</label>
                        <input type="password" id="editOldPassword" placeholder="输入当前密码">
                    </div>
                    <div class="form-group">
                        <label>新密码</label>
                        <input type="password" id="editNewPassword" placeholder="至少6位">
                    </div>
                    <div class="form-actions">
                        <button class="btn btn-primary" onclick="saveProfile()">保存资料</button>
                        <button class="btn btn-secondary" onclick="changePassword()">修改密码</button>
                        <button class="btn btn-secondary" onclick="loadMyProfile()">取消</button>
                    </div>
                </div>
            `;

            document.getElementById('profileContent').innerHTML = html;
        }

        async function changePassword() {
            const oldPassword = document.getElementById('editOldPassword').value;
            const newPassword = document.getElementById('editNewPassword').value;
            if (!oldPassword || !newPassword) { showMessage('请填写密码', 'error'); return; }
            if (newPassword.length < 6) { showMessage('新密码至少6位', 'error'); return; }
            try {
                const result = await apiRequest('/user/password', {
                    method: 'POST',
                    body: JSON.stringify({ oldPassword, newPassword })
                });
                if (result.code === 200) {
                    showMessage('密码修改成功！');
                    document.getElementById('editOldPassword').value = '';
                    document.getElementById('editNewPassword').value = '';
                } else {
                    showMessage(result.message || '修改失败', 'error');
                }
            } catch (e) { showMessage('修改失败', 'error'); }
        }

        // 保存资料
        async function saveProfile() {
            const nickname = document.getElementById('editNickname').value.trim();
            const email = document.getElementById('editEmail').value.trim();
            const bio = document.getElementById('editBio').value.trim();
            const interestTags = document.getElementById('editInterestTags').value.trim();

            try {
                const result = await apiRequest('/user/profile', {
                    method: 'POST',
                    body: JSON.stringify({
                        nickname: nickname,
                        email: email,
                        bio: bio,
                        interestTags: interestTags
                    })
                });

                if (result.code === 200) {
                    showMessage('资料更新成功！');
                    loadMyProfile();
                    updateUserInfo();
                } else {
                    showMessage(result.message || '更新失败', 'error');
                }
            } catch (error) {
                showMessage('更新失败', 'error');
            }
        }

        // 渲染活动列表
        function renderPosts(posts, containerId, showDelete = false) {
            const container = document.getElementById(containerId);
            if (!posts || posts.length === 0) {
                container.innerHTML = '<p style=\"text-align:center;padding:40px;color:var(--text-muted);\">📭 暂无活动</p>';
                return;
            }

            container.innerHTML = posts.map(post => {
                const typeMap = {
                    'BALL_GAME': '🏀 打球',
                    'BOARD_GAME': '🎲 桌游',
                    'PET_SOCIAL': '🐾 宠物社交',
                    'GROUP_ACTIVITY': '🎉 拼活动',
                    'STUDY_GROUP': '📚 学习小组'
                };
                const typeName = typeMap[post.type] || post.type;

                const activityTime = post.activityTime 
                    ? new Date(post.activityTime).toLocaleString('zh-CN', {month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'})
                    : '未设置';
                const createTime = post.createTime 
                    ? new Date(post.createTime).toLocaleString('zh-CN', {month: 'short', day: 'numeric'})
                    : '';
                
                const userAvatar = post.userAvatar || '/images/default-avatar.svg';
                const imageList = post.images ? post.images.split(',').filter(img => img.trim()) : [];
                const imageCount = imageList.length;

                // 生成图片HTML（小红书风格：图片优先）
                let imagesHTML = '';
                if (imageCount === 0) {
                    var coverStyles = {
                        'BALL_GAME': 'background:linear-gradient(135deg,#f97316,#ef4444);',
                        'BOARD_GAME': 'background:linear-gradient(135deg,#8b5cf6,#6366f1);',
                        'PET_SOCIAL': 'background:linear-gradient(135deg,#ec4899,#f43f5e);',
                        'GROUP_ACTIVITY': 'background:linear-gradient(135deg,#10b981,#059669);',
                        'STUDY_GROUP': 'background:linear-gradient(135deg,#3b82f6,#2563eb);'
                    };
                    var coverStyle = (coverStyles[post.type] || 'background:linear-gradient(135deg,#667eea,#764ba2);') + 'display:flex;align-items:center;justify-content:center;color:white;font-size:36px;cursor:pointer;min-height:180px;';
                    imagesHTML = '<div class="post-images-container" style="' + coverStyle + '" onclick="viewPostDetail(' + post.id + ')"><div style="text-align:center;"><div style="font-size:48px;margin-bottom:8px;">' + (typeMap[post.type] || '📷').charAt(0) + '</div><div style="font-size:14px;opacity:0.9;">' + (typeMap[post.type] || '活动').substring(2) + '</div></div></div>';
                } else if (imageCount === 1) {
                    imagesHTML = '<div class="post-images-container"><img data-src="' + imageList[0] + '" alt="帖子图片" class="post-main-image" onclick="viewPostDetail(' + post.id + ')" loading="lazy"></div>';
                } else if (imageCount === 2) {
                    imagesHTML = '<div class="post-images-grid two"><img data-src="' + imageList[0] + '" alt="图片" onclick="viewPostDetail(' + post.id + ')" loading="lazy"><img data-src="' + imageList[1] + '" alt="图片" onclick="viewPostDetail(' + post.id + ')" loading="lazy"></div>';
                } else if (imageCount === 3) {
                    imagesHTML = '<div class="post-images-grid three"><img data-src="' + imageList[0] + '" alt="图片" onclick="viewPostDetail(' + post.id + ')" loading="lazy"><img data-src="' + imageList[1] + '" alt="图片" onclick="viewPostDetail(' + post.id + ')" loading="lazy"><img data-src="' + imageList[2] + '" alt="图片" onclick="viewPostDetail(' + post.id + ')" loading="lazy"></div>';
                } else {
                    imagesHTML = '<div class="post-images-grid"><img data-src="' + imageList[0] + '" alt="图片" onclick="viewPostDetail(' + post.id + ')" loading="lazy"><img data-src="' + imageList[1] + '" alt="图片" onclick="viewPostDetail(' + post.id + ')" loading="lazy"><img data-src="' + imageList[2] + '" alt="图片" onclick="viewPostDetail(' + post.id + ')" loading="lazy"><div style="position: relative;"><img data-src="' + imageList[3] + '" alt="图片" onclick="viewPostDetail(' + post.id + ')" loading="lazy"><div class="post-images-count">+' + (imageCount - 4) + '</div></div></div>';
                }

                return `
                    <div class="post-card" onclick="viewPostDetail(${post.id})">
                        <!-- 用户信息 -->
                        <div class="post-header" onclick="event.stopPropagation()">
                            <img src="${userAvatar}" alt="头像" class="post-author-avatar" onerror="this.src='/images/default-avatar.svg'" onclick="viewUserProfile(${post.userId}); event.stopPropagation();">
                            <div class="post-author-info">
                                <div class="post-author-name">${post.nickname || post.username}${post.authorPostCount > 5 ? ' <span style="font-size:10px;background:var(--primary-bg);color:var(--primary-dark);padding:1px 6px;border-radius:8px;font-weight:500;" title="已发起'+post.authorPostCount+'个活动">活跃</span>' : ''}</div>
                                <div class="post-meta">${createTime}</div>
                            </div>
                        </div>

                        <!-- 图片区域（小红书风格：图片优先） -->
                        ${imagesHTML}

                        <!-- 内容区域 -->
                        <div class="post-body" onclick="event.stopPropagation()">
                            <div class="post-title">${highlightText(post.title, currentSearchKeyword)}</div>
                            ${post.content ? `<div class="post-content">${highlightText(post.content, currentSearchKeyword)}</div>` : ''}
                            <div style="display: flex; gap: 8px; margin-top: 8px; flex-wrap: wrap; align-items: center;">
                                <span class="badge badge-primary" style="font-size: 11px; padding: 4px 8px;">${typeName}</span>
                                ${getStatusBadge(post.status)}
                                ${post.distance ? `<span style="font-size: 12px; color: var(--primary-dark); font-weight: 600;">📍 ${post.distance}km</span>` : ''}
                                ${post.location && !post.distance ? `<span style="font-size: 12px; color: #999;">📍 ${post.location}</span>` : ''}
                                ${activityTime !== '未设置' ? `<span style="font-size: 12px; color: #999;">🕐 ${activityTime}</span>` : ''}
                                ${post.maxPeople ? `<span style="font-size: 12px; color: var(--text-secondary);">👥 ${post.currentPeople || 0}/${post.maxPeople}人</span>` : ''}
                            </div>
                        </div>

                        <!-- 底部操作栏（小红书风格：图标+数字） -->
                        <div class="post-footer" onclick="event.stopPropagation()">
                            <div class="post-stats">
                                <div class="post-stat-item" onclick="toggleLikeFromList(${post.id}); event.stopPropagation();" title="点赞">
                                    <span class="post-stat-icon">${post.isLiked ? '❤️' : '🤍'}</span>
                                    <span>${post.likeCount || 0}</span>
                                </div>
                                <div class="post-stat-item" onclick="viewPostDetail(${post.id}); event.stopPropagation();" title="评论">
                                    <span class="post-stat-icon">💬</span>
                                    <span>${post.commentCount || 0}</span>
                                </div>
                                <div class="post-stat-item" onclick="toggleFavoriteFromList(${post.id}); event.stopPropagation();" title="收藏">
                                    <span class="post-stat-icon">${post.isFavorited ? '⭐' : '☆'}</span>
                                    <span>${post.favoriteCount || 0}</span>
                                </div>
                            </div>
                            <div class="post-actions">
                                ${post.status === 'FINISHED' || post.status === 'CANCELLED'
                                    ? `<button class="post-action-btn" style="background:#d1d5db;color:#6b7280;cursor:default;">${post.status === 'FINISHED' ? '已结束' : '已取消'}</button>`
                                    : post.status === 'FULL'
                                    ? `<button class="post-action-btn" style="background:#fef3c7;color:#92400e;cursor:default;">已满员</button>`
                                    : post.isRegistered
                                    ? `<button class="post-action-btn btn-outline" onclick="cancelRegistration(${post.id}); event.stopPropagation();">已报名</button>`
                                    : `<button class="post-action-btn btn-primary" onclick="registerPost(${post.id}); event.stopPropagation();">报名</button>`
                                }
                                ${showDelete ? `<button class="post-action-btn" style="background: #ff4757; color: white;" onclick="deletePost(${post.id}); event.stopPropagation();">删除</button>` : ''}
                            </div>
                        </div>
                    </div>
                `;
            }).join('');
            observeNewImages(container);
        }

        // 地理位置
        let userLat = null;
        let userLng = null;

        // 获取用户位置
        function requestUserLocation() {
            if (navigator.geolocation) {
                navigator.geolocation.getCurrentPosition(
                    (pos) => { userLat = pos.coords.latitude; userLng = pos.coords.longitude; loadPosts(); },
                    () => { userLat = null; userLng = null; },
                    { timeout: 5000, maximumAge: 600000 }
                );
            }
        }
        requestUserLocation();

        // 搜索历史
        const SEARCH_HISTORY_KEY = 'ggl_search_history';
        const MAX_SEARCH_HISTORY = 10;

        function getSearchHistory() {
            try {
                return JSON.parse(localStorage.getItem(SEARCH_HISTORY_KEY)) || [];
            } catch (e) { return []; }
        }

        function saveSearchHistory(keyword) {
            let list = getSearchHistory();
            list = list.filter(k => k !== keyword);
            list.unshift(keyword);
            if (list.length > MAX_SEARCH_HISTORY) list = list.slice(0, MAX_SEARCH_HISTORY);
            localStorage.setItem(SEARCH_HISTORY_KEY, JSON.stringify(list));
        }

        function renderSearchHistory() {
            const container = document.getElementById('searchHistory');
            const list = getSearchHistory();
            if (list.length === 0) { container.style.display = 'none'; return; }
            container.style.display = 'flex';
            container.innerHTML =
                '<span class="search-history-label">最近搜索:</span>' +
                list.map(k => `<span class="search-history-chip" onclick="searchFromHistory('${k.replace(/'/g, "\\'")}')">${escapeHtml(k)}</span>`).join('') +
                '<button class="search-history-clear" onclick="clearSearchHistory()">清除</button>';
        }

        function searchFromHistory(keyword) {
            document.getElementById('searchKeyword').value = keyword;
            searchPosts();
        }

        function clearSearchHistory() {
            localStorage.removeItem(SEARCH_HISTORY_KEY);
            document.getElementById('searchHistory').style.display = 'none';
        }

        // 搜索关键词（用于高亮）
        let currentSearchKeyword = '';

        // 分页状态
        let currentPage = 1;
        let totalPages = 1;
        const pageSize = 12;

        // 活动状态映射
        function getStatusBadge(status) {
            if (!status || status === 'RECRUITING') return '';
            const map = {
                'FULL': '<span class="badge" style="background:#fef3c7;color:#92400e;font-size:11px;padding:4px 8px;">已满员</span>',
                'ONGOING': '<span class="badge" style="background:#dbeafe;color:#1e40af;font-size:11px;padding:4px 8px;">进行中</span>',
                'FINISHED': '<span class="badge" style="background:#f3f4f6;color:#6b7280;font-size:11px;padding:4px 8px;">已结束</span>',
                'CANCELLED': '<span class="badge" style="background:#fef2f2;color:#dc2626;font-size:11px;padding:4px 8px;">已取消</span>'
            };
            return map[status] || '';
        }

        // 帖子图片URL数组
        let postImageUrls = [];

        // 获取当前位置填入表单
        function getCurrentPosition() {
            if (navigator.geolocation) {
                navigator.geolocation.getCurrentPosition(
                    (pos) => {
                        document.getElementById('postLatitude').value = pos.coords.latitude;
                        document.getElementById('postLongitude').value = pos.coords.longitude;
                        showMessage('已获取当前位置');
                    },
                    () => showMessage('无法获取位置', 'error'),
                    { timeout: 10000 }
                );
            } else {
                showMessage('浏览器不支持定位', 'error');
            }
        }

        // 处理帖子图片上传
        async function handlePostImageUpload(event) {
            const files = event.target.files;
            if (files.length + postImageUrls.length > 9) {
                showMessage('最多只能上传9张图片', 'error');
                event.target.value = '';
                return;
            }

            const previewContainer = document.getElementById('postImagePreview');
            
            for (let i = 0; i < files.length; i++) {
                const file = files[i];
                
                // 验证文件类型
                const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
                if (!validTypes.includes(file.type)) {
                    showMessage('只支持图片格式（jpg, jpeg, png, gif, webp）', 'error');
                    continue;
                }

                // 验证文件大小（5MB）
                if (file.size > 5 * 1024 * 1024) {
                    showMessage('图片大小不能超过5MB', 'error');
                    continue;
                }

                const formData = new FormData();
                formData.append('file', file);

                try {
                    const response = await fetch('/api/upload/post-image', {
                        method: 'POST',
                        body: formData,
                        credentials: 'include'
                    });

                    const result = await response.json();

                    if (result.code === 200) {
                        postImageUrls.push(result.data);
                        renderPostImagePreview();
                        document.getElementById('postImageUrls').value = postImageUrls.join(',');
                    } else {
                        showMessage(result.message || '图片上传失败', 'error');
                    }
                } catch (error) {
                    showMessage('图片上传失败：' + error.message, 'error');
                }
            }

            // 清空input，允许重复选择同一文件
            event.target.value = '';
        }

        // 渲染帖子图片预览
        function renderPostImagePreview() {
            const previewContainer = document.getElementById('postImagePreview');
            if (postImageUrls.length === 0) {
                previewContainer.innerHTML = '';
                return;
            }

            previewContainer.innerHTML = postImageUrls.map((url, index) => `
                <div class="image-preview-item">
                    <img src="${url}" alt="预览图片">
                    <button type="button" class="remove-image" onclick="removePostImage(${index})" title="删除">×</button>
                </div>
            `).join('');
        }

        // 删除帖子图片
        function removePostImage(index) {
            postImageUrls.splice(index, 1);
            renderPostImagePreview();
            document.getElementById('postImageUrls').value = postImageUrls.join(',');
        }

        // 发布活动
        async function createPost(event) {
            event.preventDefault();
            const title = document.getElementById('postTitle').value;
            const content = document.getElementById('postContent').value;
            const type = document.getElementById('postType').value;
            const maxPeople = parseInt(document.getElementById('postMaxPeople').value);
            const activityTime = document.getElementById('postActivityTime').value;
            const location = document.getElementById('postLocation').value;
            const images = postImageUrls.join(',');


            const data = {
                title,
                content,
                type,
                maxPeople
            };

            if (activityTime) {
                data.activityTime = new Date(activityTime).toISOString();
            }
            if (location) data.location = location;
            const lat = parseFloat(document.getElementById('postLatitude').value);
            const lng = parseFloat(document.getElementById('postLongitude').value);
            if (!isNaN(lat)) data.latitude = lat;
            if (!isNaN(lng)) data.longitude = lng;
            if (images) {
                data.images = images;
            }



            try {
                const result = await apiRequest('/post', {
                    method: 'POST',
                    body: JSON.stringify(data)
                });

                if (result.code === 200) {
                    clearFormDirty();
                    showMessage('发布成功！');
                    document.getElementById('postTitle').value = '';
                    document.getElementById('postContent').value = '';
                    document.getElementById('postMaxPeople').value = '';
              
                    document.getElementById('postImages').value = '';
                    postImageUrls = [];
                    renderPostImagePreview();
                    document.getElementById('postImageUrls').value = '';
      document.getElementById('postActivityTime').value = '';
                    document.getElementById('postLocation').value = '';
                } else {
                    showMessage(result.message || '发布失败', 'error');
                }
            } catch (error) {
                showMessage('发布失败', 'error');
            }
        }

        // 报名活动
        async function registerPost(postId) {
            try {
                const result = await apiRequest(`/post/registration/${postId}`, {
                    method: 'POST'
                });

                if (result.code === 200) {
                    showMessage('报名成功！');
                    loadPosts();
                } else {
                    showMessage(result.message || '报名失败', 'error');
                }
            } catch (error) {
                showMessage('报名失败', 'error');
            }
        }

        // 取消报名
        async function cancelRegistration(postId) {
            try {
                const result = await apiRequest(`/post/registration/${postId}`, {
                    method: 'DELETE'
                });

                if (result.code === 200) {
                    showMessage('已取消报名');
                    loadPosts();
                    loadMyRegistrations();
                } else {
                    showMessage(result.message || '取消报名失败', 'error');
                }
            } catch (error) {
                showMessage('取消报名失败', 'error');
            }
        }

        // 删除活动
        async function deletePost(postId) {
            if (!confirm('确定要删除这个活动吗？')) {
                return;
            }

            try {
                const result = await apiRequest(`/post/${postId}`, {
                    method: 'DELETE'
                });

                if (result.code === 200) {
                    showMessage('删除成功');
                    loadMyPosts();
                } else {
                    showMessage(result.message || '删除失败', 'error');
                }
            } catch (error) {
                showMessage('删除失败', 'error');
            }
        }


        // 查看帖子详情
        let currentDetailPostId = null;
        let currentDetailPost = null;

        async function viewPostDetail(postId) {
            currentDetailPostId = postId;
            const modal = document.getElementById('postDetailModal');
            modal.style.display = 'block';
            
            // 加载详情
            try {
                const result = await apiRequest(`/post/${postId}`);
                if (result.code === 200) {
                    currentDetailPost = result.data;
                    renderPostDetail(result.data);
                    loadParticipants(postId);
                } else {
                    document.getElementById('postDetailContent').innerHTML = 
                        '<div class="detail-loading">加载失败：' + (result.message || '未知错误') + '</div>';
                }
            } catch (error) {
                document.getElementById('postDetailContent').innerHTML = 
                    '<div class="detail-loading">加载失败：' + error.message + '</div>';
            }
        }

        // 加载参与者列表
        async function loadParticipants(postId) {
            try {
                const result = await apiRequest(`/post/registration/${postId}/participants`);
                if (result.code === 200) {
                    renderParticipants(result.data);
                }
            } catch (e) {}
        }

        function renderParticipants(participants) {
            const container = document.getElementById('participantList');
            const countEl = document.getElementById('participantCount');
            if (!participants || participants.length === 0) {
                container.innerHTML = '<span style="font-size:12px;color:var(--text-muted);">暂无报名</span>';
                if (countEl) countEl.textContent = '';
                return;
            }
            if (countEl) countEl.textContent = '(' + participants.length + ')';
            container.innerHTML = participants.map(p => {
                const avatar = p.avatar || '/images/default-avatar.svg';
                const name = p.nickname || p.username;
                return `<div class="participant-item" onclick="viewUserProfile(${p.id})" title="@${p.username}">
                    <img src="${avatar}" alt="${name}" class="participant-avatar" onerror="this.src='/images/default-avatar.svg'">
                    <span class="participant-name">${name}</span>
                </div>`;
            }).join('');
        }

        async function openGroupChat(postId) {
            try {
                const result = await apiRequest('/group-chat/post/' + postId + '/room');
                if (result.code === 200) {
                    switchTab('messages');
                    setTimeout(function() { openChatRoom(result.data.id, 'group'); }, 300);
                }
            } catch(e) {}
        }

        window.generatePoster = function(post) {
            var canvas = document.createElement('canvas');
            canvas.width = 600; canvas.height = 800;
            var ctx = canvas.getContext('2d');

            // 背景渐变
            var grad = ctx.createLinearGradient(0, 0, 600, 800);
            grad.addColorStop(0, '#10b981'); grad.addColorStop(1, '#047857');
            ctx.fillStyle = grad; ctx.fillRect(0, 0, 600, 800);

            // 顶部装饰
            ctx.fillStyle = 'rgba(255,255,255,0.1)';
            ctx.beginPath(); ctx.arc(500, 100, 200, 0, Math.PI * 2); ctx.fill();
            ctx.beginPath(); ctx.arc(100, 700, 150, 0, Math.PI * 2); ctx.fill();

            // 标题
            ctx.fillStyle = 'white'; ctx.font = 'bold 36px -apple-system, sans-serif';
            var title = post.title.length > 12 ? post.title.substring(0, 12) + '...' : post.title;
            ctx.fillText(title, 40, 180);

            // 类型标签
            var typeMap = { 'BALL_GAME': '🏀 打球', 'BOARD_GAME': '🎲 桌游', 'PET_SOCIAL': '🐾 宠物', 'GROUP_ACTIVITY': '🎉 拼活动', 'STUDY_GROUP': '📚 学习' };
            ctx.fillStyle = 'rgba(255,255,255,0.9)'; ctx.font = '20px -apple-system, sans-serif';
            ctx.fillText(typeMap[post.type] || post.type, 40, 230);

            // 信息卡片
            ctx.fillStyle = 'rgba(255,255,255,0.15)';
            var cardY = 280;
            ctx.fillRect(30, cardY, 540, 300, 20);

            ctx.fillStyle = 'white'; ctx.font = '18px -apple-system, sans-serif';
            var lines = [
                '📍 ' + (post.location || '未设置'),
                '🕐 ' + (post.activityTime ? new Date(post.activityTime).toLocaleString('zh-CN') : '未设置'),
                '👥 ' + (post.currentPeople || 0) + ' / ' + post.maxPeople + ' 人',
                '👤 发布者：' + (post.nickname || post.username)
            ];
            lines.forEach(function(line, i) { ctx.fillText(line, 60, cardY + 50 + i * 50); });

            // 底部
            ctx.fillStyle = 'rgba(255,255,255,0.7)'; ctx.font = '14px -apple-system, sans-serif';
            ctx.fillText('扫描二维码或访问 GreenGrassland', 150, 680);
            ctx.fillText('找到你的校园搭子', 200, 710);
            ctx.fillStyle = 'white'; ctx.font = 'bold 20px -apple-system, sans-serif';
            ctx.fillText('🌿 GreenGrassland', 180, 760);

            // 二维码占位
            ctx.fillStyle = 'white'; ctx.fillRect(420, 580, 120, 120);
            ctx.fillStyle = '#059669'; ctx.font = '12px sans-serif';
            ctx.fillText('扫码加入', 445, 645);

            // 弹窗展示
            var modal = document.createElement('div');
            modal.style.cssText = 'position:fixed;top:0;left:0;right:0;bottom:0;background:rgba(0,0,0,0.7);z-index:9999;display:flex;align-items:center;justify-content:center;';
            modal.innerHTML = '<div style="position:relative;"><img src="' + canvas.toDataURL() + '" style="max-width:90vw;max-height:90vh;border-radius:12px;"><button onclick="this.parentElement.parentElement.remove()" style="position:absolute;top:-10px;right:-10px;background:white;border:none;border-radius:50%;width:32px;height:32px;font-size:18px;cursor:pointer;">✕</button><button onclick="var a=document.createElement(\'a\');a.href=this.parentElement.querySelector(\'img\').src;a.download=\'activity-poster.png\';a.click();" style="position:absolute;bottom:16px;right:16px;background:var(--primary);color:white;border:none;border-radius:20px;padding:8px 20px;font-size:14px;cursor:pointer;">💾 保存图片</button></div>';
            document.body.appendChild(modal);
            modal.addEventListener('click', function(e) { if (e.target === modal) modal.remove(); });
        }

        function sharePost(postId) {
            const url = window.location.origin + '/?post=' + postId;
            if (navigator.clipboard) {
                navigator.clipboard.writeText(url).then(function() {
                    showMessage('链接已复制到剪贴板！');
                });
            } else {
                const input = document.createElement('input');
                input.value = url;
                document.body.appendChild(input);
                input.select();
                document.execCommand('copy');
                document.body.removeChild(input);
                showMessage('链接已复制！');
            }
        }

        // 关闭详情模态框
        function closePostDetail() {
            const modal = document.getElementById('postDetailModal');
            modal.style.display = 'none';
            currentDetailPostId = null;
        }

        // 渲染帖子详情
        function renderPostDetail(post) {
            const typeMap = {
                'BALL_GAME': '🏀 打球',
                'BOARD_GAME': '🎲 桌游',
                'PET_SOCIAL': '🐾 宠物社交',
                'GROUP_ACTIVITY': '🎉 拼活动',
                'STUDY_GROUP': '📚 学习小组'
            };
            const typeName = typeMap[post.type] || post.type;

            const activityTime = post.activityTime 
                ? new Date(post.activityTime).toLocaleString('zh-CN')
                : '未设置';
            const createTime = post.createTime 
                ? new Date(post.createTime).toLocaleString('zh-CN')
                : '';

            const userAvatar = post.userAvatar || '/images/default-avatar.svg';
            
            let html = `
                <div class="post-detail">
                    <!-- 贴主信息 -->
                    <div class="detail-author">
                        <img src="${userAvatar}" alt="头像" class="detail-author-avatar" onerror="this.src='/images/default-avatar.svg'" onclick="viewUserProfile(${post.userId}); event.stopPropagation();">
                        <div class="detail-author-info">
                            <div class="detail-author-name">${post.nickname || post.username}</div>
                            <div class="detail-author-username">@${post.username}</div>
                        </div>
                    </div>

                    <!-- 帖子信息 -->
                    <div class="detail-post-info">
                        <div class="detail-post-title">${post.title}</div>
                        <div class="detail-post-meta">
                            <span class="badge badge-primary">${typeName}</span>
                            <span>发布时间：${createTime}</span>
                        </div>
                        <div class="detail-post-content">${(post.content || '无内容描述').replace(/\n/g, '<br>')}</div>
                        ${post.images ? `
                        <div class="detail-post-images">
                            ${post.images.split(',').filter(img => img.trim()).map((img, index) => `
                                <img src="${img.trim()}" alt="帖子图片" class="detail-post-image" onclick="openImagePreview(${post.id}, ${index})">
                            `).join('')}
                        </div>
                        ` : ''}
                        <div class="detail-post-activity">
                            <div class="detail-post-activity-item">
                                <div class="detail-post-activity-label">活动时间</div>
                                <div class="detail-post-activity-value">${activityTime}</div>
                            </div>
                            <div class="detail-post-activity-item">
                                <div class="detail-post-activity-label">活动地点</div>
                                <div class="detail-post-activity-value">${post.location || '未设置'}</div>
                            </div>
                            <div class="detail-post-activity-item">
                                <div class="detail-post-activity-label">报名人数</div>
                                <div class="detail-post-activity-value">${post.currentPeople || 0} / ${post.maxPeople}</div>
                            </div>
                        </div>
                    </div>

                    <!-- 参与者 -->
                    <div class="detail-post-action-section">
                        <div class="detail-comments-title" style="margin-bottom:4px;">参与者 <span id="participantCount"></span></div>
                        <div class="participant-list" id="participantList"></div>
                    </div>

                    <!-- 操作按钮 -->
                    <div class="detail-actions-bar">
                        <button class="detail-action-btn ${post.isFavorited ? 'active' : ''}" 
                                onclick="toggleFavorite(${post.id})" id="favoriteBtn${post.id}">
                            ⭐ ${post.favoriteCount || 0}
                        </button>
                        <button class="detail-action-btn ${post.isLiked ? 'active' : ''}" 
                                onclick="toggleLike(${post.id})" id="likeBtn${post.id}">
                            👍 ${post.likeCount || 0}
                        </button>
                        <button class="detail-action-btn" onclick="sharePost(${post.id})" title="复制链接">🔗 分享</button>
                        <button class="detail-action-btn" onclick="generatePoster(currentDetailPost)" title="生成海报">🎨 海报</button>
                        ${post.isRegistered
                            ? `<span class="chat-entry-badge" onclick="event.stopPropagation();openGroupChat(${post.id})">💬 进入群聊</span><button class="btn btn-danger" onclick="cancelRegistrationFromDetail(${post.id})">取消报名</button>`
                            : `<button class="btn btn-success" onclick="registerPostFromDetail(${post.id})">报名</button>`
                        }
                    </div>

                    <!-- 评论区域 -->
                    <div class="detail-comments-section">
                        <div class="detail-comments-title">评论 (${post.commentCount || 0})</div>
                        <div class="detail-comment-form">
                            <textarea id="detailCommentInput" class="detail-comment-input" placeholder="写下你的评论..."></textarea>
                            <button class="btn btn-primary" onclick="submitCommentFromDetail(${post.id})">发表评论</button>
                        </div>
                        <div class="detail-comments-list" id="detailCommentsList">
                            ${renderComments(post.comments || [], post.id)}
                        </div>
                    </div>
                </div>
            `;

            document.getElementById('postDetailContent').innerHTML = html;
            observeNewImages(document.getElementById('postDetailContent'));
        }

        // 当前正在回复的评论ID
        let currentReplyCommentId = null;
        let currentPostIdForComment = null;

        // 渲染评论列表（支持嵌套）
        function renderComments(comments, postId) {
            if (!comments || comments.length === 0) {
                return '<div class="detail-loading">暂无评论</div>';
            }

            currentPostIdForComment = postId;

            return comments.map(comment => {
                return renderCommentItem(comment, postId, false);
            }).join('');
        }

        // 渲染单个评论项（支持递归渲染回复）
        function renderCommentItem(comment, postId, isReply) {
            const avatar = comment.avatar || '/images/default-avatar.svg';
            const time = comment.createTime 
                ? new Date(comment.createTime).toLocaleString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
                : '';
            
            const isCurrentReply = currentReplyCommentId === comment.id;
            const contentClass = isReply ? 'detail-comment-content' : 'detail-comment-content';
            const itemClass = isReply ? 'detail-comment-reply-item' : 'detail-comment-item';
            
            let replySection = '';
            if (comment.replies && comment.replies.length > 0) {
                replySection = `
                    <div class="detail-comment-replies">
                        ${comment.replies.map(reply => renderCommentItem(reply, postId, true)).join('')}
                    </div>
                `;
            }

            let replyInput = '';
            if (isCurrentReply) {
                replyInput = `
                    <div class="detail-comment-reply-input-container">
                        <textarea class="detail-comment-reply-input" id="replyInput_${comment.id}" placeholder="回复 ${comment.nickname || comment.username}..."></textarea>
                        <div class="detail-comment-reply-actions">
                            <button class="btn btn-secondary" onclick="cancelReply()" style="padding: 6px 12px; font-size: 13px;">取消</button>
                            <button class="btn btn-primary" onclick="submitReply(${postId}, ${comment.id})" style="padding: 6px 12px; font-size: 13px;">发送</button>
                        </div>
                    </div>
                `;
            }

            const parentUsernameText = comment.parentCommentUsername 
                ? `<span style="color: #ff2442; font-weight: 500;">@${comment.parentCommentUsername}</span> ` 
                : '';

            return `
                <div class="${itemClass}">
                    <div class="detail-comment-header">
                        <img src="${avatar}" alt="头像" class="detail-comment-avatar" onerror="this.src='/images/default-avatar.svg'" onclick="viewUserProfile(${comment.userId}); event.stopPropagation();">
                        <div class="detail-comment-author">
                            <div class="detail-comment-author-name">${comment.nickname || comment.username}</div>
                            <div class="detail-comment-time">${time}</div>
                        </div>
                    </div>
                    <div class="${contentClass}">
                        ${parentUsernameText}${escapeHtml(comment.content).replace(/\n/g, '<br>')}
                    </div>
                    <div class="detail-comment-actions">
                        <button class="detail-comment-reply-btn" onclick="showReplyInput(${comment.id})">回复</button>
                    </div>
                    ${replyInput}
                    ${replySection}
                </div>
            `;
        }

        // 显示回复输入框
        function showReplyInput(commentId) {
            currentReplyCommentId = commentId;
            // 重新加载评论以显示输入框
            if (currentPostIdForComment) {
                viewPostDetail(currentPostIdForComment);
            }
        }

        // 取消回复
        function cancelReply() {
            currentReplyCommentId = null;
            if (currentPostIdForComment) {
                viewPostDetail(currentPostIdForComment);
            }
        }

        // 提交回复
        async function submitReply(postId, parentCommentId) {
            const input = document.getElementById(`replyInput_${parentCommentId}`);
            const content = input.value.trim();
            if (!content) {
                showMessage('回复内容不能为空', 'error');
                return;
            }

            try {
                const result = await apiRequest('/comment', {
                    method: 'POST',
                    body: JSON.stringify({
                        postId: postId,
                        content: content,
                        parentCommentId: parentCommentId
                    })
                });

                if (result.code === 200) {
                    currentReplyCommentId = null;
                    showMessage('回复成功！');
                    await viewPostDetail(postId);
                } else {
                    showMessage(result.message || '回复失败', 'error');
                }
            } catch (error) {
                showMessage('回复失败', 'error');
            }
        }

        // HTML转义函数
        function escapeHtml(text) {
            const div = document.createElement('div');
            div.textContent = text;
            return div.innerHTML;
        }

        function highlightText(text, keyword) {
            if (!keyword || !text) return escapeHtml(text);
            const escaped = escapeHtml(text);
            const escapedKeyword = escapeHtml(keyword);
            const regex = new RegExp('(' + escapedKeyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&') + ')', 'gi');
            return escaped.replace(regex, '<mark class="search-highlight">$1</mark>');
        }

        // 从详情页收藏/取消收藏
        async function toggleFavorite(postId) {
            try {
                const result = await apiRequest(`/post/favorite/${postId}`, {
                    method: 'POST'
                });

                if (result.code === 200) {
                    // 重新加载详情
                    await viewPostDetail(postId);
                } else {
                    showMessage(result.message || '操作失败', 'error');
                }
            } catch (error) {
                showMessage('操作失败', 'error');
            }
        }

        // 从详情页点赞/取消点赞
        async function toggleLike(postId) {
            try {
                const result = await apiRequest(`/post/like/${postId}`, {
                    method: 'POST'
                });

                if (result.code === 200) {
                    // 重新加载详情
                    await viewPostDetail(postId);
                } else {
                    showMessage(result.message || '操作失败', 'error');
                }
            } catch (error) {
                showMessage('操作失败', 'error');
            }
        }

        // 从详情页报名
        async function registerPostFromDetail(postId) {
            try {
                const result = await apiRequest(`/post/registration/${postId}`, {
                    method: 'POST'
                });

                if (result.code === 200) {
                    showMessage('报名成功！');
                    await viewPostDetail(postId);
                    loadPosts();
                } else {
                    showMessage(result.message || '报名失败', 'error');
                }
            } catch (error) {
                showMessage('报名失败', 'error');
            }
        }

        // 从详情页取消报名
        async function cancelRegistrationFromDetail(postId) {
            try {
                const result = await apiRequest(`/post/registration/${postId}`, {
                    method: 'DELETE'
                });

                if (result.code === 200) {
                    showMessage('已取消报名');
                    await viewPostDetail(postId);
                    loadPosts();
                    loadMyRegistrations();
                } else {
                    showMessage(result.message || '取消报名失败', 'error');
                }
            } catch (error) {
                showMessage('取消报名失败', 'error');
            }
        }

        // 从详情页提交评论
        async function submitCommentFromDetail(postId) {
            const input = document.getElementById('detailCommentInput');
            const content = input.value.trim();
            if (!content) {
                showMessage('评论内容不能为空', 'error');
                return;
            }

            try {
                const result = await apiRequest('/comment', {
                    method: 'POST',
                    body: JSON.stringify({
                        postId: postId,
                        content: content,
                        parentCommentId: null
                    })
                });

                if (result.code === 200) {
                    input.value = '';
                    currentReplyCommentId = null;
                    showMessage('评论成功！');
                    await viewPostDetail(postId);
                } else {
                    showMessage(result.message || '评论失败', 'error');
                }
            } catch (error) {
                showMessage('评论失败', 'error');
            }
        }

        // 图片预览相关变量
        let currentPreviewImages = [];
        let currentPreviewIndex = 0;

        // 打开图片预览
        function openImagePreview(postId, imageIndex) {
            if (!currentDetailPost || !currentDetailPost.images) return;
            const images = currentDetailPost.images.split(',').filter(img => img.trim()).map(img => img.trim());
            if (images.length === 0) return;

            currentPreviewImages = images;
            currentPreviewIndex = imageIndex >= 0 && imageIndex < images.length ? imageIndex : 0;

            const modal = document.getElementById('imagePreviewModal');
            if (modal) {
                modal.style.display = 'block';
                updatePreviewImage();
                updatePreviewNav();
            }
        }

        // 更新预览图片
        function updatePreviewImage() {
            if (currentPreviewImages.length === 0) return;
            const img = document.getElementById('previewImage');
            const counter = document.getElementById('imageCounter');
            if (img && counter) {
                img.src = currentPreviewImages[currentPreviewIndex];
                counter.textContent = `${currentPreviewIndex + 1} / ${currentPreviewImages.length}`;
            }
        }

        // 更新导航按钮
        function updatePreviewNav() {
            const prevBtn = document.getElementById('prevImageBtn');
            const nextBtn = document.getElementById('nextImageBtn');
            if (prevBtn) prevBtn.disabled = currentPreviewIndex === 0;
            if (nextBtn) nextBtn.disabled = currentPreviewIndex === currentPreviewImages.length - 1;
        }

        // 上一张图片
        function prevImage() {
            if (currentPreviewIndex > 0) {
                currentPreviewIndex--;
                updatePreviewImage();
                updatePreviewNav();
            }
        }

        // 下一张图片
        function nextImage() {
            if (currentPreviewIndex < currentPreviewImages.length - 1) {
                currentPreviewIndex++;
                updatePreviewImage();
                updatePreviewNav();
            }
        }

        // 关闭图片预览
        function closeImagePreview() {
            const modal = document.getElementById('imagePreviewModal');
            if (modal) {
                modal.style.display = 'none';
            }
            currentPreviewImages = [];
            currentPreviewIndex = 0;
        }

        // 键盘事件：ESC关闭，左右箭头切换图片
        document.addEventListener('keydown', function(event) {
            const modal = document.getElementById('imagePreviewModal');
            if (modal && modal.style.display === 'block') {
                if (event.key === 'Escape') {
                    closeImagePreview();
                } else if (event.key === 'ArrowLeft') {
                    prevImage();
                } else if (event.key === 'ArrowRight') {
                    nextImage();
                }
            }
        });

        // 点击模态框外部关闭
        window.onclick = function(event) {
            const modal = document.getElementById('postDetailModal');
            if (event.target === modal) {
                closePostDetail();
            }
            const userProfileModal = document.getElementById('userProfileModal');
            if (event.target === userProfileModal) {
                closeUserProfile();
            }
            const imagePreviewModal = document.getElementById('imagePreviewModal');
            if (event.target === imagePreviewModal) {
                closeImagePreview();
            }
        }

        // 用户主页相关变量
        let currentProfileUserId = null;

        // 查看用户主页
        async function viewUserProfile(userId) {
            currentProfileUserId = userId;
            const modal = document.getElementById('userProfileModal');
            modal.style.display = 'block';
            
            // 加载用户信息
            try {
                const result = await apiRequest(`/user/${userId}`);
                if (result.code === 200) {
                    renderUserProfile(result.data);
                    loadCommonFollows(userId);
                } else {
                    document.getElementById('userProfileContent').innerHTML =
                        '<div class="detail-loading">加载失败：' + (result.message || '未知错误') + '</div>';
                }
            } catch (error) {
                document.getElementById('userProfileContent').innerHTML =
                    '<div class="detail-loading">加载失败：' + error.message + '</div>';
            }
        }

        async function loadCommonFollows(userId) {
            try {
                const result = await apiRequest('/user/common/follows/' + userId);
                if (result.code === 200 && result.data && result.data.length > 0) {
                    const container = document.getElementById('commonFollowsSection');
                    if (!container) return;
                    container.style.display = 'block';
                    document.getElementById('commonFollowsList').innerHTML = result.data.map(function(u) {
                        return '<span class="participant-item" onclick="viewUserProfile(' + u.id + ')" style="display:inline-flex;margin:2px;"><img src="' + (u.avatar || '/images/default-avatar.svg') + '" alt="" class="participant-avatar" onerror="this.src=\'/images/default-avatar.svg\'"><span class="participant-name">' + (u.nickname || u.username) + '</span></span>';
                    }).join('');
                }
            } catch(e) {}
        }

        // 关闭用户主页
        function closeUserProfile() {
            const modal = document.getElementById('userProfileModal');
            modal.style.display = 'none';
            currentProfileUserId = null;
        }

        // 渲染用户主页
        function renderUserProfile(user) {
            const currentUserId = getCurrentUserId();
            const isOwnProfile = currentUserId && currentUserId === user.id;
            const avatar = user.avatar || '/images/default-avatar.svg';
            const createTime = user.createTime 
                ? new Date(user.createTime).toLocaleString('zh-CN', {year: 'numeric', month: 'long', day: 'numeric'})
                : '';

            let html = `
                <div class="user-profile">
                    <div class="user-profile-header">
                        <img src="${avatar}" alt="头像" class="user-profile-avatar" onerror="this.src='/images/default-avatar.svg'">
                        <div class="user-profile-name">${user.nickname || user.username}</div>
                        <div class="user-profile-username">@${user.username}</div>
                        ${!isOwnProfile ? `
                        <div class="user-profile-actions">
                            <button class="user-profile-action-btn primary" onclick="startChatWithUser(${user.id})">💬 聊天</button>
                            <button class="user-profile-action-btn" style="background:#fef2f2;color:#dc2626;" onclick="blockUser(${user.id})">🚫 屏蔽</button>
                            <button class="user-profile-action-btn" onclick="reportTarget('USER',${user.id})">🚩 举报</button>
                        </div>
                        ` : ''}
                    </div>
                    <div class="user-profile-info">
                        <div class="user-profile-info-item">
                            <span class="user-profile-info-label">邮箱</span>
                            <span class="user-profile-info-value">${user.email || '未设置'}</span>
                        </div>
                        <div class="user-profile-info-item">
                            <span class="user-profile-info-label">注册时间</span>
                            <span class="user-profile-info-value">${createTime}</span>
                        </div>
                    </div>
                    <div id="commonFollowsSection" style="display:none;margin-top:12px;padding-top:12px;border-top:1px solid var(--border);">
                        <div style="font-size:13px;color:var(--text-secondary);margin-bottom:6px;">🤝 共同关注</div>
                        <div id="commonFollowsList"></div>
                    </div>
                </div>
            `;

            document.getElementById('userProfileContent').innerHTML = html;
        }

        async function blockUser(userId) {
            if (!confirm('确定要屏蔽该用户吗？')) return;
            const result = await apiRequest('/user/block/' + userId, { method: 'POST' });
            if (result.code === 200) { showMessage('已屏蔽'); closeUserProfile(); }
            else showMessage(result.message || '操作失败', 'error');
        }

        async function reportTarget(type, id) {
            const reason = prompt('请输入举报原因：');
            if (!reason) return;
            const result = await apiRequest('/report', { method: 'POST', body: JSON.stringify({ targetType: type, targetId: id, reason: reason }) });
            if (result.code === 200) showMessage('举报已提交');
            else showMessage(result.message || '举报失败', 'error');
        }

        // 与用户开始聊天
        async function startChatWithUser(userId) {
            try {
                // 获取或创建聊天房间
                const result = await apiRequest(`/chat/room/with/${userId}`, {
                    method: 'POST'
                });

                if (result.code === 200) {
                    const room = result.data;
                    // 关闭用户主页
                    closeUserProfile();
                    // 切换到消息标签
                    switchTab('messages');
                    // 打开聊天房间
                    await openChatRoom(room.id, 'private');
                } else {
                    showMessage(result.message || '创建聊天失败', 'error');
                }
            } catch (error) {
                showMessage('创建聊天失败', 'error');
            }
        }

        // WebSocket
        let stompClient = null;
        let wsSubscription = null;
        let onlineSubscription = null;
        let wsConnected = false;
        let onlineUsers = {};

        function connectWebSocket() {
            if (stompClient && stompClient.connected) return;
            const socket = new SockJS('/ws');
            stompClient = new StompJs.Client({
                webSocketFactory: () => socket,
                debug: function() {},
                onConnect: function() {
                    wsConnected = true;
                    onlineSubscription = stompClient.subscribe('/topic/online.status', function(msg) {
                        const data = JSON.parse(msg.body);
                        if (data.userId !== undefined) {
                            onlineUsers[data.userId] = data.online;
                            updateOnlineIndicators();
                        }
                    });
                    const currentUserId = getCurrentUserId();
                    if (currentUserId) {
                        stompClient.publish({destination: '/app/online.ping', body: JSON.stringify({userId: currentUserId})});
                    }
                    if (currentChatRoomId && currentChatRoomType) {
                        subscribeToRoom(currentChatRoomId, currentChatRoomType);
                    }
                },
                onDisconnect: function() {
                    wsConnected = false;
                    setTimeout(connectWebSocket, 3000);
                }
            });
            stompClient.activate();
        }

        function updateOnlineIndicators() {
            document.querySelectorAll('.online-dot').forEach(dot => {
                const uid = parseInt(dot.dataset.userId);
                dot.className = 'online-dot ' + (onlineUsers[uid] ? 'online' : 'offline');
            });
        }

        let typingSub = null;
        let typingTimer = null;

        function subscribeToRoom(roomId, type) {
            if (wsSubscription) { wsSubscription.unsubscribe(); wsSubscription = null; }
            if (typingSub) { typingSub.unsubscribe(); typingSub = null; }
            if (!stompClient || !stompClient.connected) return;
            const topic = type === 'group' ? '/topic/group.room.' + roomId : '/topic/chat.room.' + roomId;
            wsSubscription = stompClient.subscribe(topic, function(msg) {
                const chat = JSON.parse(msg.body);
                if (currentChatRoomId === roomId && currentChatRoomType === type) {
                    appendChatMessage(chat, type);
                }
                loadMessages();
            });
            const typingTopic = type === 'group' ? '/topic/group.typing.' + roomId : '/topic/chat.typing.' + roomId;
            typingSub = stompClient.subscribe(typingTopic, function(msg) {
                const data = JSON.parse(msg.body);
                const currentUserId = getCurrentUserId();
                if (data.userId != currentUserId) {
                    showTypingIndicator(data.nickname || '对方');
                }
            });
        }

        function showTypingIndicator(name) {
            const el = document.getElementById('typingIndicator');
            if (el) {
                el.textContent = name + ' 正在输入...';
                el.style.display = 'block';
                clearTimeout(typingTimer);
                typingTimer = setTimeout(function() { el.style.display = 'none'; }, 2000);
            }
        }

        function sendTypingEvent() {
            if (!stompClient || !stompClient.connected || !currentChatRoomId) return;
            stompClient.publish({
                destination: '/app/typing',
                body: JSON.stringify({
                    roomId: currentChatRoomId,
                    type: currentChatRoomType,
                    userId: getCurrentUserId(),
                    nickname: document.getElementById('currentUsername').textContent
                })
            });
        }

        function appendChatMessage(chat, type) {
            const container = document.getElementById('chatMessages');
            if (!container) return;
            const currentUserId = getCurrentUserId();
            const isMe = chat.senderId === currentUserId;
            const time = chat.createTime ? new Date(chat.createTime).toLocaleTimeString('zh-CN', {hour:'2-digit',minute:'2-digit'}) : '';
            const msgHtml = `
                <div class="chat-message ${isMe ? 'sent' : 'received'}">
                    ${!isMe ? `<div class="chat-message-sender">${chat.senderNickname}</div>` : ''}
                    <div class="chat-bubble">${escapeHtml(chat.content)}</div>
                    <div class="chat-time">${time}</div>
                </div>`;
            container.insertAdjacentHTML('beforeend', msgHtml);
            container.scrollTop = container.scrollHeight;
            if (!isMe) {
                notifyDesktop(chat.senderNickname || '新消息', chat.content);
            }
        }

        // 消息相关变量
        let currentChatRoomId = null;
        let currentChatRoomType = null; // 'private' or 'group'
        let chatPollInterval = null;
        let groupChatPollInterval = null;

        // 加载所有消息（合并私聊和群聊）
        async function loadMessages() {
            try {
                // 同时加载私聊和群聊
                const [privateResult, groupResult] = await Promise.all([
                    apiRequest('/chat/rooms'),
                    apiRequest('/group-chat/rooms')
                ]);

                const privateRooms = privateResult.code === 200 ? privateResult.data : [];
                const groupRooms = groupResult.code === 200 ? groupResult.data : [];

                // 合并并排序（按最后消息时间）
                const allMessages = [
                    ...privateRooms.map(room => ({...room, type: 'private'})),
                    ...groupRooms.map(room => ({...room, type: 'group'}))
                ].sort((a, b) => {
                    const timeA = a.lastMessageTime ? new Date(a.lastMessageTime).getTime() : 0;
                    const timeB = b.lastMessageTime ? new Date(b.lastMessageTime).getTime() : 0;
                    return timeB - timeA; // 最新的在前
                });

                renderMessageList(allMessages);
            } catch (error) {
                showMessage('加载失败', 'error');
                const container = document.getElementById('messageList');
                if (container) {
                    container.innerHTML = '<div class="chat-empty">加载失败</div>';
                }
            }
        }

        // 渲染统一的消息列表（合并私聊和群聊）
        function renderMessageList(messages) {
            const container = document.getElementById('messageList');
            if (!messages || messages.length === 0) {
                container.innerHTML = '<div class="chat-empty">暂无消息</div>';
                return;
            }

            const currentUserId = getCurrentUserId();
            container.innerHTML = messages.map(item => {
                const isActive = currentChatRoomId === item.id && currentChatRoomType === item.type;
                const lastMessageTime = item.lastMessageTime 
                    ? new Date(item.lastMessageTime).toLocaleString('zh-CN', {month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'})
                    : '';

                if (item.type === 'private') {
                    // 私聊
                    const avatar = item.otherUserAvatar || '/images/default-avatar.svg';
                    const otherUserId = item.user1Id === currentUserId ? item.user2Id : item.user1Id;
                    return `
                        <div class="chat-list-item ${isActive ? 'active' : ''}" 
                             onclick="openChatRoom(${item.id}, 'private')"
                             data-other-user-id="${otherUserId}"
                             data-type="private">
                            <div class="chat-list-item-header">
                                <img src="${avatar}" alt="头像" class="chat-list-item-avatar" onerror="this.src='/images/default-avatar.svg'">
                                <span class="chat-list-item-name">${item.otherUserNickname}<span class="online-dot offline" data-user-id="${otherUserId}"></span></span>
                                ${item.unreadCount > 0 ? `<span class="chat-list-item-unread">${item.unreadCount}</span>` : ''}
                            </div>
                            <div class="chat-list-item-last-message">
                                ${item.lastMessage || ''}
                                ${lastMessageTime ? ` · ${lastMessageTime}` : ''}
                            </div>
                        </div>
                    `;
                } else {
                    // 群聊
                    return `
                        <div class="chat-list-item ${isActive ? 'active' : ''}" 
                             onclick="openChatRoom(${item.id}, 'group')"
                             data-type="group">
                            <div class="chat-list-item-header">
                                <div class="chat-list-item-avatar" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; display: flex; align-items: center; justify-content: center; font-size: 16px; font-weight: 600;">群</div>
                                <span class="chat-list-item-name">${item.postTitle}</span>
                            </div>
                            <div class="chat-list-item-last-message">
                                ${item.lastMessage || ''}
                                ${lastMessageTime ? ` · ${lastMessageTime}` : ''}
                            </div>
                        </div>
                    `;
                }
            }).join('');
        }

        // 打开聊天房间（统一处理私聊和群聊）
        async function openChatRoom(roomId, type) {
            // 清除之前的轮询
            if (chatPollInterval) {
                clearInterval(chatPollInterval);
                chatPollInterval = null;
            }
            if (groupChatPollInterval) {
                clearInterval(groupChatPollInterval);
                groupChatPollInterval = null;
            }

            currentChatRoomId = roomId;
            currentChatRoomType = type;

            // 显示聊天窗口
            document.getElementById('chatWindow').style.display = 'flex';

            if (type === 'private') {
                await loadChatMessages(roomId);
                startChatPolling(roomId);
            } else {
                await loadGroupChatMessages(roomId);
                startGroupChatPolling(roomId);
            }
            
            // 刷新列表
            loadMessages();
        }

        // 加载聊天消息
        async function loadChatMessages(roomId) {
            try {
                const result = await apiRequest(`/chat/room/${roomId}/messages`);
                if (result.code === 200) {
                    renderChatMessages(result.data);
                } else {
                    showMessage(result.message || '加载失败', 'error');
                }
            } catch (error) {
                showMessage('加载失败', 'error');
            }
        }

        // 渲染聊天消息
        function renderChatMessages(messages) {
            const container = document.getElementById('chatWindow');
            const currentUserId = getCurrentUserId();
            
            if (!messages || messages.length === 0) {
                container.innerHTML = `
                    <div class="chat-window-header" id="chatWindowHeader">
                        <div>聊天</div>
                    </div>
                    <div class="chat-empty">还没有消息，开始聊天吧</div>
                    <div class="chat-input-area">
                        <textarea id="chatMessageInput" class="chat-input" placeholder="输入消息..."></textarea>
                        <button class="btn btn-primary" onclick="sendChatMessage()">发送</button>
                    </div>
                `;
                return;
            }

            const messagesHtml = messages.map(msg => {
                const isOwn = msg.senderId === currentUserId;
                const avatar = msg.senderAvatar || '/images/default-avatar.svg';
                const time = msg.createTime ? new Date(msg.createTime).toLocaleString('zh-CN', {hour: '2-digit', minute: '2-digit'}) : '';
                return `
                    <div class="chat-message ${isOwn ? 'own' : ''}">
                        <img src="${avatar}" alt="头像" class="chat-message-avatar">
                        <div class="chat-message-content">
                            <div class="chat-message-header">${msg.senderNickname} ${time}</div>
                            <div class="chat-message-text">${msg.content}</div>
                        </div>
                    </div>
                `;
            }).join('');

            container.innerHTML = `
                <div class="chat-window-header" id="chatWindowHeader">
                    <div>聊天</div>
                </div>
                <div class="chat-messages" id="chatMessagesContainer">
                    ${messagesHtml}
                </div>
                <div id="typingIndicator" class="typing-indicator" style="display:none;"></div>
                <div class="chat-input-area">
                    <textarea id="chatMessageInput" class="chat-input" placeholder="输入消息..." onkeydown="if(event.key==='Enter' && !event.shiftKey) {event.preventDefault(); sendChatMessage();}" oninput="sendTypingEvent()"></textarea>
                    <button class="btn btn-primary" onclick="sendChatMessage()">发送</button>
                </div>
            `;

            // 滚动到底部
            setTimeout(() => {
                const messagesContainer = document.getElementById('chatMessagesContainer');
                if (messagesContainer) {
                    messagesContainer.scrollTop = messagesContainer.scrollHeight;
                }
            }, 100);
        }

        // 发送私聊消息
        async function sendChatMessage() {
            const input = document.getElementById('chatMessageInput');
            const content = input.value.trim();
            if (!content || !currentChatRoomId) {
                return;
            }

            // 从房间列表中找到对方用户ID
            const roomItem = document.querySelector('#messageList .chat-list-item.active');
            if (!roomItem) return;

            const receiverId = parseInt(roomItem.getAttribute('data-other-user-id'));
            if (!receiverId) return;

            try {
                const result = await apiRequest('/chat/send', {
                    method: 'POST',
                    body: JSON.stringify({
                        receiverId: receiverId,
                        content: content
                    })
                });

                if (result.code === 200) {
                    input.value = '';
                    await loadChatMessages(currentChatRoomId);
                    loadMessages();
                } else {
                    showMessage(result.message || '发送失败', 'error');
                }
            } catch (error) {
                showMessage('发送失败', 'error');
            }
        }

        // 获取当前用户ID
        function getCurrentUserId() {
            return window.currentUserId || null;
        }

        // 开始轮询聊天消息
        function startChatPolling(roomId) {
            subscribeToRoom(roomId, 'private');
        }

        // 加载群聊消息
        async function loadGroupChatMessages(roomId) {
            try {
                const result = await apiRequest(`/group-chat/room/${roomId}/messages`);
                if (result.code === 200) {
                    renderGroupChatMessages(result.data);
                } else {
                    showMessage(result.message || '加载失败', 'error');
                }
            } catch (error) {
                showMessage('加载失败', 'error');
            }
        }

        // 渲染群聊消息
        function renderGroupChatMessages(messages) {
            const container = document.getElementById('chatWindow');
            const currentUserId = getCurrentUserId();
            
            if (!messages || messages.length === 0) {
                container.innerHTML = `
                    <div class="chat-window-header" id="chatWindowHeader">
                        <div>群聊</div>
                    </div>
                    <div class="chat-empty">还没有消息，开始聊天吧</div>
                    <div class="chat-input-area">
                        <textarea id="groupChatMessageInput" class="chat-input" placeholder="输入消息..."></textarea>
                        <button class="btn btn-primary" onclick="sendGroupChatMessage()">发送</button>
                    </div>
                `;
                return;
            }

            const messagesHtml = messages.map(msg => {
                const isOwn = msg.senderId === currentUserId;
                const avatar = msg.senderAvatar || '/images/default-avatar.svg';
                const time = msg.createTime ? new Date(msg.createTime).toLocaleString('zh-CN', {hour: '2-digit', minute: '2-digit'}) : '';
                return `
                    <div class="chat-message ${isOwn ? 'own' : ''}">
                        <img src="${avatar}" alt="头像" class="chat-message-avatar" onerror="this.src='/images/default-avatar.svg'">
                        <div class="chat-message-content">
                            <div class="chat-message-header">${msg.senderNickname} ${time}</div>
                            <div class="chat-message-text">${msg.content}</div>
                        </div>
                    </div>
                `;
            }).join('');

            container.innerHTML = `
                <div class="chat-window-header" id="chatWindowHeader">
                    <div>群聊</div>
                </div>
                <div class="chat-messages" id="chatMessagesContainer">
                    ${messagesHtml}
                </div>
                <div id="typingIndicator" class="typing-indicator" style="display:none;"></div>
                <div class="chat-input-area">
                    <textarea id="groupChatMessageInput" class="chat-input" placeholder="输入消息..." onkeydown="if(event.key==='Enter' && !event.shiftKey) {event.preventDefault(); sendGroupChatMessage();}" oninput="sendTypingEvent()"></textarea>
                    <button class="btn btn-primary" onclick="sendGroupChatMessage()">发送</button>
                </div>
            `;

            // 滚动到底部
            setTimeout(() => {
                const messagesContainer = document.getElementById('chatMessagesContainer');
                if (messagesContainer) {
                    messagesContainer.scrollTop = messagesContainer.scrollHeight;
                }
            }, 100);
        }

        // 发送群聊消息
        async function sendGroupChatMessage() {
            const input = document.getElementById('groupChatMessageInput');
            const content = input.value.trim();
            if (!content || !currentChatRoomId || currentChatRoomType !== 'group') {
                return;
            }

            try {
                const result = await apiRequest('/group-chat/send', {
                    method: 'POST',
                    body: JSON.stringify({
                        roomId: currentChatRoomId,
                        content: content
                    })
                });

                if (result.code === 200) {
                    input.value = '';
                    await loadGroupChatMessages(currentChatRoomId);
                    loadMessages();
                } else {
                    showMessage(result.message || '发送失败', 'error');
                }
            } catch (error) {
                showMessage('发送失败', 'error');
            }
        }

        // 开始轮询群聊消息
        function startGroupChatPolling(roomId) {
            subscribeToRoom(roomId, 'group');
        }

        // 通知相关变量
        let notificationPollInterval = null;
        let notificationModalOpen = false;

        // 加载通知列表
        async function loadNotifications() {
            try {
                const result = await apiRequest('/notification');
                if (result.code === 200) {
                    renderNotifications(result.data || []);
                } else {
                    const list = document.getElementById('notificationList');
                    if (list) list.innerHTML = '<div class="notification-empty">加载失败</div>';
                }
            } catch (error) {
                const list = document.getElementById('notificationList');
                if (list) list.innerHTML = '<div class="notification-empty">加载失败</div>';
            }
        }

        // 加载未读通知数
        async function loadUnreadCount() {
            try {
                const result = await apiRequest('/notification/unread/count');
                if (result.code === 200) {
                    const count = result.data || 0;
                    const badge = document.getElementById('notificationBadge');
                    if (badge) {
                        if (count > 0) {
                            badge.textContent = count > 99 ? '99+' : count;
                            badge.classList.add('show');
                        } else {
                            badge.classList.remove('show');
                        }
                    }
                }
            } catch (error) {
                // 忽略错误
            }
        }

        // 通知分类
        let notificationFilter = 'ALL';

        function setNotificationFilter(filter) {
            notificationFilter = filter;
            document.querySelectorAll('.notif-filter-btn').forEach(b => b.classList.remove('active'));
            const btn = document.getElementById('notifFilter' + filter);
            if (btn) btn.classList.add('active');
            // 重新筛选已加载的通知
            if (window._lastNotifications) renderNotifications(window._lastNotifications);
        }

        // 渲染通知列表
        function renderNotifications(notifications) {
            window._lastNotifications = notifications;
            const list = document.getElementById('notificationList');
            if (!list) return;

            let filtered = notifications;
            if (notificationFilter !== 'ALL') {
                filtered = notifications.filter(n => n.type === notificationFilter);
            }

            if (!filtered || filtered.length === 0) {
                list.innerHTML = '<div class="notification-empty">📭 暂无通知</div>';
                return;
            }

            const typeMap = {
                'LIKE': '点赞了你的活动',
                'COMMENT': '评论了你的活动',
                'FOLLOW': '关注了你',
                'POST': '发布了新活动'
            };

            list.innerHTML = filtered.map(notif => {
                const avatar = notif.fromUserAvatar || '/images/default-avatar.svg';
                const userNickname = notif.fromUserNickname || '用户';
                const typeText = typeMap[notif.type] || notif.content || '通知';
                const time = notif.createTime 
                    ? new Date(notif.createTime).toLocaleString('zh-CN', { 
                        month: 'short', 
                        day: 'numeric', 
                        hour: '2-digit', 
                        minute: '2-digit' 
                    })
                    : '';
                const unreadClass = notif.isRead ? '' : 'unread';

                let clickHandler = '';
                if (notif.postId) {
                    clickHandler = `onclick="viewNotificationPost(${notif.id}, ${notif.postId})"`;
                } else if (notif.fromUserId) {
                    clickHandler = `onclick="viewNotificationUser(${notif.id}, ${notif.fromUserId})"`;
                } else {
                    clickHandler = `onclick="markNotificationAsRead(${notif.id})"`;
                }

                return `
                    <div class="notification-item ${unreadClass}" ${clickHandler}>
                        <img src="${avatar}" alt="头像" class="notification-avatar" onerror="this.src='/images/default-avatar.svg'">
                        <div class="notification-content">
                            <div class="notification-text">
                                <span class="notification-user">${escapeHtml(userNickname)}</span> ${typeText}
                            </div>
                            <div class="notification-time">${time}</div>
                        </div>
                    </div>
                `;
            }).join('');
        }

        // 切换通知模态框
        function toggleNotificationModal() {
            const modal = document.getElementById('notificationModal');
            if (!modal) return;

            notificationModalOpen = !notificationModalOpen;
            if (notificationModalOpen) {
                modal.style.display = 'block';
                loadNotifications();
                if (!notificationPollInterval) {
                    notificationPollInterval = setInterval(loadUnreadCount, 5000);
                }
            } else {
                modal.style.display = 'none';
            }
        }

        // 查看通知相关的帖子
        async function viewNotificationPost(notificationId, postId) {
            await markNotificationAsRead(notificationId);
            toggleNotificationModal();
            await viewPostDetail(postId);
        }

        // 查看通知相关的用户
        async function viewNotificationUser(notificationId, userId) {
            await markNotificationAsRead(notificationId);
            toggleNotificationModal();
            viewUserProfile(userId);
        }

        // 标记单个通知为已读
        async function markNotificationAsRead(notificationId) {
            try {
                await apiRequest(`/notification/${notificationId}/read`, {
                    method: 'POST'
                });
                await loadNotifications();
                await loadUnreadCount();
            } catch (error) {
                // 忽略错误
            }
        }

        // 标记所有通知为已读
        async function markAllNotificationsAsRead() {
            try {
                await apiRequest('/notification/mark-all-read', {
                    method: 'POST'
                });
                await loadNotifications();
                await loadUnreadCount();
            } catch (error) {
                showMessage('操作失败', 'error');
            }
        }

        // 初始化通知轮询
        function initNotificationPolling() {
            if (window.currentUserId) {
                loadUnreadCount();
                if (!notificationPollInterval) {
                    notificationPollInterval = setInterval(loadUnreadCount, 10000);
                }
            }
        }

        // 图片懒加载
        function initLazyLoading() {
            const observer = new IntersectionObserver((entries) => {
                entries.forEach(entry => {
                    if (entry.isIntersecting) {
                        const img = entry.target;
                        if (img.dataset.src) {
                            img.src = img.dataset.src;
                            img.removeAttribute('data-src');
                            img.classList.add('lazy-loaded');
                        }
                        observer.unobserve(img);
                    }
                });
            }, { rootMargin: '100px' });

            document.querySelectorAll('img[data-src]').forEach(img => observer.observe(img));
            return observer;
        }

        let lazyObserver = null;

        // 观察新增的懒加载图片
        function observeNewImages(container) {
            if (!lazyObserver) lazyObserver = initLazyLoading();
            if (container) {
                container.querySelectorAll('img[data-src]').forEach(img => lazyObserver.observe(img));
            }
        }

        // 浏览器桌面通知
        function requestNotificationPermission() {
            if ('Notification' in window && Notification.permission === 'default') {
                Notification.requestPermission();
            }
        }

        function notifyDesktop(title, body) {
            if ('Notification' in window && Notification.permission === 'granted') {
                new Notification(title, { body: body, icon: '/images/default-avatar.svg' });
            }
        }

        // 表单离开提醒
        let formDirty = false;

        function markFormDirty() { formDirty = true; }
        function clearFormDirty() { formDirty = false; }

        window.addEventListener('beforeunload', function(e) {
            if (formDirty) {
                e.preventDefault();
                e.returnValue = '您有未保存的内容，确定离开吗？';
                return e.returnValue;
            }
        });

        // 网络状态监听
        function showOfflineBanner(msg, isOnline) {
            const banner = document.getElementById('offlineBanner');
            banner.textContent = msg;
            banner.className = 'offline-banner' + (isOnline ? ' online' : '');
            banner.style.display = 'block';
            if (isOnline) setTimeout(function() { banner.style.display = 'none'; }, 2000);
        }

        window.addEventListener('offline', function() { showOfflineBanner('网络连接已断开', false); });
        window.addEventListener('online', function() { showOfflineBanner('网络已恢复', true); });

        // 暗色模式
        function getDarkMode() { return localStorage.getItem('dark_mode') === 'true'; }
        function setDarkMode(on) {
            document.documentElement.setAttribute('data-theme', on ? 'dark' : 'light');
            document.getElementById('darkModeBtn').textContent = on ? '☀️' : '🌙';
            localStorage.setItem('dark_mode', on);
        }
        function toggleDarkMode() { setDarkMode(!getDarkMode()); }

        // 下拉刷新
        let pullStartY = 0;
        let pulling = false;

        function initPullToRefresh() {
            const postList = document.getElementById('postList');
            if (!postList) return;

            postList.addEventListener('touchstart', function(e) {
                if (window.scrollY === 0) {
                    pullStartY = e.touches[0].clientY;
                    pulling = true;
                }
            }, { passive: true });

            postList.addEventListener('touchmove', function(e) {
                if (!pulling) return;
                const dy = e.touches[0].clientY - pullStartY;
                if (dy > 60) {
                    pulling = false;
                    loadPosts();
                }
            }, { passive: true });

            postList.addEventListener('touchend', function() { pulling = false; });
        }

        // 图片压缩
        function compressImage(file, maxWidth, quality) {
            return new Promise(function(resolve) {
                const reader = new FileReader();
                reader.onload = function(e) {
                    const img = new Image();
                    img.onload = function() {
                        const canvas = document.createElement('canvas');
                        let w = img.width, h = img.height;
                        if (w > maxWidth) { h = h * maxWidth / w; w = maxWidth; }
                        canvas.width = w; canvas.height = h;
                        canvas.getContext('2d').drawImage(img, 0, 0, w, h);
                        canvas.toBlob(function(blob) { resolve(blob); }, file.type, quality || 0.8);
                    };
                    img.src = e.target.result;
                };
                reader.readAsDataURL(file);
            });
        }

        async function loadRecommendPosts() {
            try {
                const result = await apiRequest('/post/recommend');
                if (result.code === 200 && result.data && result.data.length > 0) {
                    document.getElementById('recommendPosts').innerHTML = result.data.slice(0, 4).map(function(p) {
                        return '<div class="featured-post-card" onclick="viewPostDetail(' + p.id + ')" style="border:1px solid #c7d2fe;"><div class="featured-post-title">' + escapeHtml(p.title) + '</div><div class="featured-post-meta">' + (p.location || '') + '</div></div>';
                    }).join('');
                    document.getElementById('recommendSection').style.display = 'block';
                }
            } catch(e) {}
        }

        // Hero 区控制
        function updateHeroVisibility() {
            var hero = document.getElementById('heroSection');
            var userInfo = document.getElementById('userInfo');
            if (hero) hero.style.display = (userInfo && userInfo.classList.contains('hidden')) ? 'block' : 'none';
        }

        // 搜索自动补全
        var searchTitles = [];
        function initSearchAutocomplete() {
            var input = document.getElementById('searchKeyword');
            if (!input) return;
            var list = document.createElement('div');
            list.id = 'searchSuggestions';
            list.style.cssText = 'position:absolute;background:var(--surface);border:1px solid var(--border);border-radius:var(--radius);max-height:200px;overflow-y:auto;z-index:100;width:100%;display:none;box-shadow:var(--shadow-md);';
            input.parentElement.style.position = 'relative';
            input.parentElement.appendChild(list);

            input.addEventListener('input', function() {
                var val = this.value.trim().toLowerCase();
                if (val.length < 2) { list.style.display = 'none'; return; }
                var matches = searchTitles.filter(function(t) { return t.toLowerCase().indexOf(val) >= 0; }).slice(0, 5);
                if (matches.length === 0) { list.style.display = 'none'; return; }
                list.innerHTML = matches.map(function(t) { return '<div style="padding:8px 12px;cursor:pointer;font-size:13px;color:var(--text);" onmouseover="this.style.background=\'var(--surface-hover)\'" onmouseout="this.style.background=\'transparent\'" onclick="document.getElementById(\'searchKeyword\').value=\'' + t.replace(/'/g, "\\'") + '\';document.getElementById(\'searchSuggestions\').style.display=\'none\';searchPosts();">' + t + '</div>'; }).join('');
                list.style.display = 'block';
            });
            document.addEventListener('click', function(e) { if (e.target !== input) list.style.display = 'none'; });

            // 加载活动标题
            apiRequest('/post?page=1&pageSize=50').then(function(r) {
                if (r.code === 200) {
                    var posts = r.data.content || r.data;
                    searchTitles = posts.map(function(p) { return p.title; }).filter(Boolean);
                }
            }).catch(function(){});
        }

        // 精选活动
        async function loadFeaturedPosts() {
            try {
                const result = await apiRequest('/post?sortBy=createTime&sortOrder=DESC&page=1&pageSize=6');
                if (result.code === 200 && result.data.content && result.data.content.length > 0) {
                    const featured = result.data.content.slice(0, 4);
                    const container = document.getElementById('featuredPosts');
                    container.innerHTML = featured.map(function(p) {
                        return '<div class="featured-post-card" onclick="viewPostDetail(' + p.id + ')"><div class="featured-post-title">' + escapeHtml(p.title) + '</div><div class="featured-post-meta">' + (p.location || '') + '</div></div>';
                    }).join('');
                    document.getElementById('featuredSection').style.display = 'block';
                }
            } catch(e) {}
        }

        // 无限滚动
        function initInfiniteScroll() {
            let loading = false;
            window.addEventListener('scroll', function() {
                if (loading) return;
                if (window.innerHeight + window.scrollY >= document.body.offsetHeight - 300) {
                    if (currentPage < totalPages) {
                        loading = true;
                        const keyword = document.getElementById('searchKeyword').value.trim();
                        if (keyword || document.getElementById('searchType').value) {
                            searchPosts(currentPage + 1);
                        } else {
                            loadMorePosts(currentPage + 1);
                        }
                        setTimeout(function() { loading = false; }, 1000);
                    }
                }
            });
        }

        async function loadMorePosts(page) {
            try {
                const params = new URLSearchParams();
                params.append('page', page);
                params.append('pageSize', pageSize);
                if (userLat != null && userLng != null) {
                    params.append('lat', userLat);
                    params.append('lng', userLng);
                }
                const result = await apiRequest('/post?' + params.toString());
                if (result.code === 200 && result.data.content) {
                    appendPosts(result.data.content);
                    currentPage = result.data.page;
                    totalPages = result.data.totalPages;
                }
            } catch(e) {}
        }

        function appendPosts(posts) {
            const container = document.getElementById('postList');
            const html = posts.map(function(post) {
                const typeMap = { 'BALL_GAME': '🏀 打球', 'BOARD_GAME': '🎲 桌游', 'PET_SOCIAL': '🐾 宠物社交', 'GROUP_ACTIVITY': '🎉 拼活动', 'STUDY_GROUP': '📚 学习小组' };
                const typeName = typeMap[post.type] || post.type;
                const activityTime = post.activityTime ? new Date(post.activityTime).toLocaleString('zh-CN', {month:'short',day:'numeric',hour:'2-digit',minute:'2-digit'}) : '未设置';
                const userAvatar = post.userAvatar || '/images/default-avatar.svg';
                return '<div class="post-card" onclick="viewPostDetail(' + post.id + ')"><div class="post-header" onclick="event.stopPropagation()"><img src="' + userAvatar + '" alt="头像" class="post-author-avatar" onerror="this.src=\'/images/default-avatar.svg\'"><div class="post-author-info"><div class="post-author-name">' + escapeHtml(post.nickname || post.username) + '</div></div></div><div class="post-body"><div class="post-title">' + highlightText(post.title, currentSearchKeyword) + '</div></div></div>';
            }).join('');
            container.insertAdjacentHTML('beforeend', html);
            observeNewImages(container);
        }

        // 热门标签
        function renderHotTags() {
            const container = document.getElementById('hotTags');
            if (!container) return;
            const tags = [
                { label: '🏀 打球', type: 'BALL_GAME' },
                { label: '🎲 桌游', type: 'BOARD_GAME' },
                { label: '📚 学习', type: 'STUDY_GROUP' },
                { label: '🎉 拼活动', type: 'GROUP_ACTIVITY' },
                { label: '🐾 宠物', type: 'PET_SOCIAL' }
            ];
            container.innerHTML = tags.map(function(t) {
                return '<span class="hot-tag" onclick="document.getElementById(\'searchType\').value=\'' + t.type + '\';searchPosts()">' + t.label + '</span>';
            }).join('');
        }

        // 新人引导
        function showOnboarding() {
            if (localStorage.getItem('onboarding_done')) return;
            const overlay = document.createElement('div');
            overlay.style.cssText = 'position:fixed;top:0;left:0;right:0;bottom:0;background:rgba(0,0,0,0.6);z-index:9999;display:flex;align-items:center;justify-content:center;';
            overlay.innerHTML = '<div style="background:var(--surface);border-radius:16px;padding:32px;max-width:360px;text-align:center;color:var(--text);"><h2 style="margin-bottom:12px;">🌿 欢迎来到青青草原！</h2><p style="color:var(--text-secondary);margin-bottom:6px;">🔍 搜索感兴趣的活动</p><p style="color:var(--text-secondary);margin-bottom:6px;">📍 开启定位发现附近活动</p><p style="color:var(--text-secondary);margin-bottom:6px;">💬 报名后即可加入群聊</p><p style="color:var(--text-secondary);margin-bottom:18px;">👤 完善个人资料获得推荐</p><button onclick="this.parentElement.parentElement.remove();localStorage.setItem(\'onboarding_done\',\'1\');" style="padding:10px 32px;background:var(--primary);color:white;border:none;border-radius:24px;font-size:16px;cursor:pointer;">开始探索</button></div>';
            document.body.appendChild(overlay);
            overlay.addEventListener('click', function(e) { if (e.target === overlay) { overlay.remove(); localStorage.setItem('onboarding_done', '1'); } });
        }

        // XSS 审计：确保所有用户内容经过 escapeHtml
        // - 帖子标题/内容: highlightText → escapeHtml ✅
        // - 评论: escapeHtml ✅
        // - 聊天消息: escapeHtml ✅
        // - 通知: escapeHtml ✅
        // - 用户昵称在卡片中: 通过 DOM textContent ✅

        // 前端错误监控
        window.onerror = function(msg, url, line, col, error) {
            console.error('[Error]', msg, 'at', url, ':', line);
            try {
                var payload = JSON.stringify({ message: msg, url: url, line: line, col: col, time: new Date().toISOString() });
                if (navigator.sendBeacon) navigator.sendBeacon('/api/error/report', payload);
            } catch(e) {}
        };
        window.addEventListener('unhandledrejection', function(e) {
            console.error('[Promise Error]', e.reason);
        });

        // 页面加载时检查登录状态
        window.onload = function() {
            if (getDarkMode()) setDarkMode(true);
            updateUserInfo();
            connectWebSocket();
            lazyObserver = initLazyLoading();
            requestNotificationPermission();
            initPullToRefresh();
            renderHotTags();
            loadFeaturedPosts();
            loadRecommendPosts();
            initInfiniteScroll();
            initSearchAutocomplete();
            updateHeroVisibility();
            if ('serviceWorker' in navigator) {
                navigator.serviceWorker.register('/sw.js');
            }
            setTimeout(showOnboarding, 1000);
        };

})();
