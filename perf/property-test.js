import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    vus: 200,          // 동시에 200명
    duration: '30s',  // 30초 동안
};

export default function () {
    const url = 'http://localhost:8080/properties/list?isPurchased=false&page=0&size=10';

    const params = {
        headers: {
            'accept': 'application/json;charset=UTF-8',
            'Authorization': 'Bearer eyJhbGciOiJIUzUxMiJ9.eyJpYXQiOjE3NzQ4NzI3ODEsImV4cCI6MTc3NDg3NjM4MSwic3ViIjoiNTEzIiwibWVtYmVySWQiOjUxMywiYXV0aCI6IlVTRVIifQ.MXdPKmLcDkshThdyRover1nOgcyB4mjtfSFtYdPjZao0B4nYXH52AxwGv5KwoMr_0KNiO9FII6X_OSl_z7JZcw',
        },
    };

    const res = http.get(url, params);

    check(res, {
        'status is 200': (r) => r.status === 200,
    });

}