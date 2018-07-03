import axios from "axios/index";
import {setup} from 'axios-cache-adapter'

const cachedApi = setup({
    cache: {
        maxAge: 5 * 1000 //5seconds
    }
});

export function GetVideos() {
    return cachedApi.get('/videos');
}

export function PlayVideo(video) {
    return axios.post('/videos/play', {filename: video.path, audio: 'both'});
}

export function ResumeVideo() {
    return axios.get('/videos/resume');
}

export function PauseVideo() {
    return axios.get('/videos/pause');
}

export function StopVideo() {
    return axios.get('/videos/stop');
}