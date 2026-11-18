import { getStore, setStore } from '@/util/store'

const screen = {
    state: {
        customScreen: getStore({
            name: 'customScreen'
        }) || {},
        customScreenMenu: getStore({
            name: 'customScreenMenu'
        }) || [],
        updateCustomScreenMenu: false,
        customScreenInfo: {}
    },
    mutations: {
        SET_CUSTOMSCREEN: (state,playload) => {
            state['customScreen'] = playload;
            setStore({ name: 'customScreen', content: playload })
        },
        SET_CUSTOMSCREENMENU: (state,playload) => {
            state['customScreenMenu'] = playload;
            setStore({ name: 'customScreenMenu', content: playload })
        },
        SET_UPDATECUSTOMSCREENMENU: (state,playload) => {
            state['updateCustomScreenMenu'] = playload;
        },
        SET_CUSTOMSCREENINFO: (state,playload) => {
            state['customScreenInfo'] = playload;
        },
    }
}

export default screen
