<template>
  <div class="card-box" :style="{ width: width, height: height }">
    <span class="card-rect"></span>
    <div class="point"></div>
    <div class="box">
      <div class="card-content">
        <slot></slot>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "doubleCard",
  props:{
    width:{
      type: String,
      default(){
        return '300px'
      }
    },
    height:{
      type: String,
      default(){
        return '350px'
      }
    }
  }
}
</script>

<style scoped lang="scss">
.card-box {
  /*  width: 300px;
    height: 350px;*/
  position: relative;
  font-size: 14px;
  text-shadow: 0 10px 12px #001025;

  &:after{
    content: "";
    border: 3px solid #32c5ff;
    width: 40%;
    height: 70%;
    position: absolute;
    left: -1px;
    top: -1px;
    z-index: 1;
    border-right: none;
    border-bottom: none;
    border-radius: 6px 0 0 0;
  }
  &:hover{
    .point{
      opacity: 1;
      left: 40%
    }
  }

  .point{
    width: 20px;
    height: 4px;
    border-radius: 50%;
    background: #fff;
    box-shadow: 0px 0px 3px 3px rgb(138, 203, 255);
    position: absolute;
    top: -2px;
    left: +0%;
    z-index: 2;
    transition: 1s ease-in-out all;
    opacity: 0;
  }
}
.box {
  width: 100%;
  height: 100%;
  background: linear-gradient(
          180deg,
          rgba(138, 203, 255, 1),
          rgba(41, 106, 143, 1),
          rgba(190, 40, 40, 1)
  );
  border-radius: 6px;
  position: relative;
  z-index: 1;
  overflow: hidden;
  resize: both;
  min-width: 250px;
  min-height: 100px;
  color: #fff;

  &:after{
    content: "";
    width: calc(100% - 4px);
    height: calc(100% - 4px);
    position: absolute;
    background: linear-gradient(181deg, #021734 0%, #3d1328 100%);
    left: 50%;
    top: 50%;
    transform: translate(-50%, -50%);
    z-index: -1;
    border-radius: 4px;
  }

  .card-content{
    padding: 10px;
  }
}

.card-rect {
  width: 80%;
  height: 90%;
  position: absolute;
  right: -8px;
  bottom: -8px;
  border-radius: 6px;
  background: #66b5f3;
  clip-path: polygon(
          0 0,
          calc(100% - 10px) 0,
          100% 10px,
          100% 100%,
          10px 100%,
          0 calc(100% - 10px)
  );
  &:after{
    content: "";
    position: absolute;
    width: calc(100% - 3px);
    height: calc(100% - 3px);
    left: 50%;
    top: 50%;
    transform: translate(-50%, -50%);
    z-index: -1;
    background: #093258;
    border-radius: 4px;
    clip-path: polygon(
            0 0,
            calc(100% - 10px) 0,
            100% 10px,
            100% 100%,
            10px 100%,
            0 calc(100% - 10px)
    );
  }
}

</style>
