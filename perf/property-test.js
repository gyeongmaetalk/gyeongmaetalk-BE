import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    vus: 20,
    duration: '2m',
    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<1500'],
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const TOKEN = __ENV.TOKEN;

export default function () {
    const res = http.get(
        `${BASE_URL}/api/properties?page=0&size=20&isPurchased=true`,
        {
            headers: {
                Authorization: TOKEN,
            },
        }
    );

    check(res, {
        'status is 200': (r) => r.status === 200,
    });

    sleep(1);
}