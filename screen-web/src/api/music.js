import request from "../util/request.js";

const BASE_URL = "/music-api/api";

const musicRequest = (config) => {
  return request({
    ...config,
    url: BASE_URL + config.url,
    timeout: config.timeout || 15000
  })
}

export const musicApi = {
  getHotSongs() {
    return this.getPlaylistDetail(3778678)
  },

  getNewSongs() {
    return this.getPlaylistDetail(3779629)
  },

  getTopList(id = 19723756) {
    return this.getPlaylistDetail(id)
  },

  getSongUrl(id) {
    return musicRequest({
      url: "/song/enhance/player/url",
      method: "get",
      params: {
        ids: `[${id}]`,
        br: 320000
      }
    })
  },

  getSongDetail(ids) {
    return musicRequest({
      url: "/song/detail",
      method: "get",
      params: {
        ids: ids
      }
    })
  },

  search(keywords, limit = 10) {
    return musicRequest({
      url: "/search/get/web",
      method: "get",
      params: {
        keywords: keywords,
        limit: limit
      }
    })
  },

  getLyric(id) {
    return musicRequest({
      url: "/song/lyric",
      method: "get",
      params: {
        id: id
      }
    })
  },

  getPlaylistDetail(id) {
    return musicRequest({
      url: "/playlist/detail",
      method: "get",
      params: {
        id: id
      }
    })
  }
}
