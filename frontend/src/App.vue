<template>
  <div class="app">
    <header class="header">
      <div class="header-inner">
        <span class="logo">☢ АтомДежурство</span>
        <nav>
          <RouterLink to="/employees">Персонал</RouterLink>
          <RouterLink to="/shifts">Назначение смен</RouterLink>
          <RouterLink to="/schedule">График дежурств</RouterLink>
        </nav>
      </div>
    </header>
    <main class="main">
      <RouterView />
    </main>
  </div>
</template>

<style>
* { box-sizing: border-box; margin: 0; padding: 0; }
body { font-family: 'Segoe UI', sans-serif; background: #f0f2f5; color: #1a1a2e; }
.app { min-height: 100vh; }
.header {
  background: #1a1a2e;
  color: white;
  padding: 0 24px;
  height: 56px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.3);
}
.header-inner {
  width: 100%;
  max-width: 1300px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 40px;
}
.logo { font-size: 18px; font-weight: 700; letter-spacing: 0.5px; color: #4fc3f7; }
nav { display: flex; gap: 8px; }
nav a {
  color: #ccc;
  text-decoration: none;
  padding: 6px 16px;
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.2s;
}
nav a:hover { background: rgba(255,255,255,0.1); color: white; }
nav a.router-link-active { background: #4fc3f7; color: #1a1a2e; font-weight: 600; }
.main { max-width: 1300px; margin: 32px auto; padding: 0 24px; }
</style>
