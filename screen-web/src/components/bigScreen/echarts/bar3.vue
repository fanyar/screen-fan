<template>
  <div style="width: 100%; height: 100%" id="bar3"></div>
</template>

<script>
import * as echarts from 'echarts'

const fit = value => {
  if (typeof document === 'undefined') return value
  const rootFontSize = parseFloat(getComputedStyle(document.documentElement).fontSize) || 108
  return Number((value * rootFontSize / 108).toFixed(2))
}

export default {
  name: "bar3",
  data(){
    var xData = ["2020-01", "2020-02", "2020-03", "2020-04", "2020-05", "2020-06"]
    var line = ["1030", "205", "84","568","689","4587"];
    return {
      option: {
        tooltip: {
          trigger: 'item',
        },
        // graphic: [{
        //     type: 'text',
        //     z: 100,
        //     right: '4%',
        //     top: '8%',
        //     style: {
        //         fill: '#81A5E8',
        //         text: graphicData.join('\n'),
        //         padding:[20,20],
        //         font: 'italic small-caps  18px/2 cursive',

        //     }
        // }],
        grid: {
          left: '10%',
          top: '10%',
          right: '5%',
          bottom: '20%',
        },
        legend: {
          show: true,
          icon: 'circle',
          orient: 'horizontal',
          top: '90.5%',
          right: 'center',
          itemWidth: fit(16.5),
          itemHeight: fit(6),
          // itemGap: 30,
          textStyle: {
            // color: '#FFFFFF'
            color: '#C9C8CD',
            fontSize: fit(14)
          }
        },
        xAxis: [{
          data: xData,
          axisLabel: {
            textStyle: {
              color: '#aaaaaa',
              fontSize: fit(14)
            },
            margin: fit(30), //刻度标签与轴线之间的距离。
          },

          axisLine: {
            show: false //不显示x轴
          },
          axisTick: {
            show: false //不显示刻度
          },
          boundaryGap: true,
          splitLine: {
            show: false,
            width: 0.08,
            lineStyle: {
              type: "solid",
              color: "#03202E"
            }
          }
        }],
        yAxis: [{
          splitLine: {
            show: false,
            lineStyle: {
              color: '#eee',
              type: 'solid'
            }
          },
          axisTick: {
            show: false
          },
          axisLine: {
            show: false
          },
          axisLabel: {
            textStyle: {
              color: '#888'
            },
          }
        }],
        series: [
          {//柱底圆片
            name: "",
            type: "pictorialBar",
            symbolSize: [fit(60), fit(20)],
            symbolOffset: [0, fit(10)],
            z: 12,
            itemStyle: {
              "normal": {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{
                  offset: 0,
                  color: "rgba(89,211,255,1)"
                },
                  {
                    offset: 1,
                    color: "rgba(23,237,194,1)"
                  }
                ])
              }
            },
            data: line
          },

          //柱体
          {
            name: '',
            type: 'bar',
            barWidth: fit(60),
            barGap: '0%',
            itemStyle: {
              "normal": {
                "color": {
                  "x": 0,
                  "y": 0,
                  "x2": 0,
                  "y2": 1,
                  "type": "linear",
                  "global": false,
                  "colorStops": [{//第一节下面
                    "offset": 0,
                    "color": "rgba(0,255,245,0.5)"
                  }, {
                    "offset": 1,
                    "color": "#43bafe"
                  }]
                }
              }
            },

            data: line
          },

          //柱顶圆片
          {
            name: "",
            type: "pictorialBar",
            symbolSize: [fit(60), fit(20)],
            symbolOffset: [0, -fit(10)],
            z: 12,
            symbolPosition: "end",
            "itemStyle": {
              "normal": {
                color: new echarts.graphic.LinearGradient(0,0,0,1,
                    [{
                      offset: 0,
                      color: "rgba(89,211,255,1)"
                    },
                      {
                        offset: 1,
                        color: "rgba(23,237,194,1)"
                      }
                    ],
                    false
                ),
              }
            },
            data: line
          }
        ]
      }
    }
  },
  mounted() {
    this.initEcharts();
  },
  methods:{
    initEcharts(){
      this.$nextTick(() => {
        let dashEcharts = document.getElementById("bar3");
        let myChartTemp = echarts.init(dashEcharts);
        myChartTemp.setOption(this.option);
      })
    }
  }
}
</script>

<style scoped>

</style>
