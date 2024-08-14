<script setup>
defineProps({
  msg: {
    type: String,
    required: true,
    default: 'Hello'
  }
})
</script>

<template>
  <div class="title">
    <div class="container">
      <img src="../../assets/bing.ico">
      <span class="desc">每次一張相片，伴您探索世界</span>

    </div>
  </div>
</template>

<style scoped>
.title {
  display: flex;
  width: 100%;
  height: 50px;
  background: linear-gradient(to right, #ffecea, #c4e7f7, #ffecea, #c4e7f7, #ffecea);
}

.container {
  width: 60%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.desc {
  display: flex;
  align-items: center;
}
</style>
