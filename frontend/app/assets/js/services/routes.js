define(['angular', './controllers', 'common'], function (angular, controllers) {
    'use strict';

    var mod = angular.module('services.routes', ['pl.agh.edu.common']);
    mod.config(['$routeProvider', function ($routeProvider) {
        $routeProvider
            .when('/services', {
                templateUrl: '/assets/partials/services/index.html',
                controller: controllers.ServicesCtrl
            })
            .when('/services/rastrigin', {
                templateUrl: '/assets/partials/services/rastrigin.html',
                controller: controllers.RastriginCtrl
            });
    }]);
    return mod;
});
