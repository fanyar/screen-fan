import {ref} from "vue"
import { defineStore } from "pinia"

export const useCommonStore = defineStore('common', () => {
    let userName = ref('admin');
    let userPwd = ref('abc123');

    return {
        userName,
        userPwd
    }
})

