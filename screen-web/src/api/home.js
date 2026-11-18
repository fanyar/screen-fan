import request from "../util/request.js";

export const hospitalPage = params => request({
  url: "/admin/sysplatformsettings/getSettings",
  method: "get",
  params
})

