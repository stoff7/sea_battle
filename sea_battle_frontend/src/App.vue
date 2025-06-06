<script setup>
import { ref, onMounted } from 'vue'
import bgMusic from '@/assets/theme.mp3'

const bgAudio = ref(null)

function playOnUserGesture() {

    const playOnce = () => {

        if (bgAudio.value) {
            bgAudio.value
                .play()
                .then(() => {

                })
                .catch((err) => {

                })
        } else {

        }
        document.removeEventListener('click', playOnce)
    }
    document.addEventListener('click', playOnce, { once: true })
}

onMounted(() => {
    if (bgAudio.value) {
        bgAudio.value
            .play()
            .then(() => {
            })
            .catch((err) => {

                const immediatePlay = () => {

                    bgAudio.value
                        .play()
                        .then(() => {

                        })
                        .catch((error) => {
                        })
                    document.removeEventListener('click', immediatePlay)
                }
                document.addEventListener('click', immediatePlay, { once: true })
            })
    } else {
    }
    bgAudio.value.volume = 0.01

})
</script>

<template>
    <div id="app">
        <router-view />
    </div>
    <audio ref="bgAudio" :src="bgMusic" autoplay loop @canplay="playOnUserGesture"></audio>
</template>

<style scoped>
audio {
    display: none;
}
</style>
