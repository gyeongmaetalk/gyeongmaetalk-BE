import http from 'k6/http';
import { check } from 'k6';

export const options = {
    vus: 200,
    duration: '30s',
};

const BASE_URL = 'http://localhost:8080';
const TOKEN = 'eyJhbGciOiJIUzUxMiJ9.eyJpYXQiOjE3NzQ4NzI3ODEsImV4cCI6MTc3NDg3NjM4MSwic3ViIjoiNTEzIiwibWVtYmVySWQiOjUxMywiYXV0aCI6IlVTRVIifQ.MXdPKmLcDkshThdyRover1nOgcyB4mjtfSFtYdPjZao0B4nYXH52AxwGv5KwoMr_0KNiO9FII6X_OSl_z7JZcw';

export default function () {
    const res = http.get(
        `${BASE_URL}/reviews/list?type=LATEST&page=0&size=20`,
        {
            headers: {
                accept: 'application/json;charset=UTF-8',
                Authorization: `Bearer ${TOKEN}`,
            },
        }
    );

    check(res, {
        'status is 200': (r) => r.status === 200,
    });
}