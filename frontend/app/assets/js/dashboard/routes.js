/**
 * Dashboard routes.
 */
define(['angular', './controllers', 'common'], function(angular, controllers) {
  'use strict';

  var mod = angular.module('dashboard.routes', ['pl.agh.edu.common']);
  mod.config(['$routeProvider', function($routeProvider) {
    $routeProvider
      .when('/dashboard',  {templateUrl: '/assets/partials/dashboard/index.html',  controller:controllers.DashboardCtrl});
  }]);
  return mod;
});
