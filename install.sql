/*
SQLyog Ultimate v12.09 (64 bit)
MySQL - 8.0.19 : Database - hotel
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`hotel` /*!40100 DEFAULT CHARACTER SET utf8 */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `hotel`;

/*Table structure for table `rooms` */

DROP TABLE IF EXISTS `rooms`;

CREATE TABLE `rooms` (
  `roomid` int unsigned NOT NULL AUTO_INCREMENT,
  `roomtype` varchar(20) NOT NULL,
  `typeid` int unsigned NOT NULL,
  KEY `roomid` (`roomid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `rooms` */

/*Table structure for table `roomstype` */

DROP TABLE IF EXISTS `roomstype`;

CREATE TABLE `roomstype` (
  `typeid` int unsigned NOT NULL,
  `info` varchar(1000) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `roomstype` */

/*Table structure for table `staff` */

DROP TABLE IF EXISTS `staff`;

CREATE TABLE `staff` (
  `username` varchar(255) NOT NULL,
  `uid` int unsigned NOT NULL AUTO_INCREMENT,
  `role` varchar(20) NOT NULL,
  `email` varchar(255) NOT NULL,
  `phone` varchar(11) NOT NULL,
  `sex` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`uid`),
  KEY `uid` (`uid`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;

/*Data for the table `staff` */

insert  into `staff`(`username`,`uid`,`role`,`email`,`phone`,`sex`) values ('iamwwc',1,'administrator','iamwwc@gmail.com','15725508400','male'),('wwc',2,'administrator','15725508400','123456','female'),('斗宗强者',3,'administrator','iam.wuweichao@gmail.com','110','male');

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
