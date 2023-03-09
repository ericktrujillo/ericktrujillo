package com.emerik.userservice.service;

import com.emerik.userservice.entity.User;
import com.emerik.userservice.feignclients.BikeFeignClient;
import com.emerik.userservice.feignclients.CarFeignClient;
import com.emerik.userservice.model.Bike;
import com.emerik.userservice.model.Car;
import com.emerik.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    CarFeignClient carFeignClient;

    @Autowired
    BikeFeignClient bikeFeignClient;

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public User getUserById(int id) {
        return userRepository.findById(id).orElse(null);
    }

    public User save(User user) {
        User userNew = userRepository.save(user);
        return userNew;
    }

    ////Acess To Microservices by Rest///
    public List<Car> getCars(int userId) {
        List<Car> cars = restTemplate.getForObject("http://localhost:8002/car/byuser/" + userId, List.class);
        return cars;
    }

    public List<Bike> getBikes(int userId) {
        List<Bike> bikes = restTemplate.getForObject("http://localhost:8003/bike/byuser/" + userId, List.class);
        return bikes;
    }

    /// Using FeignClients////

    public Car saveCAr(int userId,Car car) {
        car.setUserId(userId);
        Car carNew = carFeignClient.save(car);
        return carNew;
    }

    public Bike saveBike(int userId, Bike bike) {
        bike.setUserId(userId);
        Bike bikeNew = bikeFeignClient.save(bike);
        return bikeNew;
    }

    public Map<String, Object> getUserAndVehicles(int userId) {
        Map<String, Object> result = new HashMap<>();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            result.put("Mensaje", "No existe el usuario");
            return result;
        }
        result.put("User", user);
        List<Car> cars = carFeignClient.getCars(userId);
        if (cars.isEmpty())
            result.put("Cars", "no tiene autos");
        else
            result.put("Cars", cars);

        List<Bike> bikes = bikeFeignClient.getBikes(userId);
        if (bikes.isEmpty())
            result.put("Bikes", "no tiene bikes");
        else
            result.put("Bikes", bikes);

        return result;
    }
}
