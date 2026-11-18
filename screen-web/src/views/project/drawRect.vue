<template>

  <div>
    <canvas id="myCanvas" width="800" height="600"></canvas>
  </div>
</template>

<script>
export default {
  name: "drawRect",
  data(){
    return {
      click: 0
    }
  },
  mounted(){
    this.init();
  },
  methods:{
    init(){
      // 获取Canvas元素并创建上下文
      const canvas = document.getElementById('myCanvas');
      const ctx = canvas.getContext('2d');

// 圆角矩形的尺寸
      const rectWidth = 200;
      const rectHeight = 100;
      const radius = 20; // 圆角的半径

// 小球的大小和长度
      const ballSize = 10;
      const ballLength = 0; // 假设长度为50

// 绘制圆角矩形
      function drawRectangle() {
        ctx.beginPath();
        ctx.moveTo(radius, 0);
        ctx.lineTo(rectWidth - radius, 0);
        ctx.quadraticCurveTo(rectWidth, 0, rectWidth, radius);
        ctx.lineTo(rectWidth, rectHeight - radius);
        ctx.quadraticCurveTo(rectWidth, rectHeight, rectWidth - radius, rectHeight);
        ctx.lineTo(radius, rectHeight);
        ctx.quadraticCurveTo(0, rectHeight, 0, rectHeight - radius);
        ctx.lineTo(0, radius);
        ctx.quadraticCurveTo(0, 0, radius, 0);
        ctx.fillStyle = 'blue';
        ctx.fill();
      }

// 绘制小球
      function drawBall(x, y) {
        ctx.beginPath();
        ctx.arc(x, y, ballSize, 0, 2 * Math.PI);
        ctx.fillStyle = 'red';
        ctx.fill();
      }

// 计算小球的位置
      function calculateBallPosition() {
        const x = radius + (rectWidth - 2 * radius - ballLength) / 2;
        const y = 0//(rectHeight - radius) / 2;
        return { x, y };
      }

// 清除画布并绘制矩形和小球
      function draw() {
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        drawRectangle();
        const position = calculateBallPosition();
        drawBall(position.x, position.y);
      }

// 调用draw函数绘制图形
      draw();
    },

  }
}
</script>

<style scoped lang="scss">
#div{
  width: 300px;
  height: 200px;
  margin-top: 20px;
  margin-left: 20px;
  border:1px solid #c3c3c3;
  position: relative;
  margin-bottom: 100px;
}
canvas{
  position: absolute;
}
</style>
