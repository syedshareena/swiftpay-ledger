import http from 'k6/http';
import { sleep, check } from 'k6';

export let options = {
  vus: 50,
  duration: '60s',
};

export default function () {
  const response = http.get('http://localhost:8081/actuator/health');
  check(response, {
    'status is 200': (r) => r.status === 200,
  });
  sleep(0.2);
}
