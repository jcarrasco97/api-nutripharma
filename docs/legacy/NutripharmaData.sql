-- MySQL dump 10.13  Distrib 5.7.15, for Win64 (x86_64)
--
-- Host: localhost    Database: mynutripharma
-- ------------------------------------------------------
-- Server version	5.7.15-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `actividades`
--

DROP TABLE IF EXISTS `actividades`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `actividades` (
  `idActividad` int(11) NOT NULL,
  `tipo` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`idActividad`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `agenda`
--

DROP TABLE IF EXISTS `agenda`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `agenda` (
  `idAgenda` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `idFarmacia` int(10) unsigned NOT NULL DEFAULT '0',
  `idNutri` int(10) unsigned NOT NULL DEFAULT '0',
  `name` varchar(50) NOT NULL DEFAULT '',
  `img` longblob,
  `fechaSubida` varchar(45) NOT NULL DEFAULT '',
  PRIMARY KEY (`idAgenda`)
) ENGINE=InnoDB AUTO_INCREMENT=1090 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `albaranes`
--

DROP TABLE IF EXISTS `albaranes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `albaranes` (
  `idAlbaran` int(11) NOT NULL,
  `fechaalbaran` date NOT NULL,
  `idNumeroAlbaran` int(11) NOT NULL,
  PRIMARY KEY (`idAlbaran`),
  KEY `FK_albaranes_idNumeroAlbaran` (`idNumeroAlbaran`),
  CONSTRAINT `FK_albaranes_idNumeroAlbaran` FOREIGN KEY (`idNumeroAlbaran`) REFERENCES `numerosalbaranes` (`idNumeroAlbaran`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `antecedentesclinicos`
--

DROP TABLE IF EXISTS `antecedentesclinicos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `antecedentesclinicos` (
  `idAntecedenteClinico` int(11) NOT NULL,
  `fecha` date DEFAULT NULL,
  `observaciones` mediumtext,
  `idPaciente` int(11) NOT NULL,
  `idTipoAntecedenteClinico` int(11) DEFAULT NULL,
  PRIMARY KEY (`idAntecedenteClinico`),
  KEY `FK_antecedentesclinicos_idTipoAntecedenteClinico` (`idTipoAntecedenteClinico`),
  KEY `FK_antecedentesclinicos_idPaciente` (`idPaciente`),
  CONSTRAINT `FK_antecedentesclinicos_idPaciente` FOREIGN KEY (`idPaciente`) REFERENCES `pacientes` (`idPaciente`),
  CONSTRAINT `FK_antecedentesclinicos_idTipoAntecedenteClinico` FOREIGN KEY (`idTipoAntecedenteClinico`) REFERENCES `tiposantecedentesclinicos` (`idTipoAntecedenteClinico`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `antecedentestratamientos`
--

DROP TABLE IF EXISTS `antecedentestratamientos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `antecedentestratamientos` (
  `idAntecedenteTratamiento` int(11) NOT NULL,
  `fecha` date NOT NULL,
  `observaciones` mediumtext,
  `idPaciente` int(11) NOT NULL,
  `idTipoAntecedenteTratamiento` int(11) DEFAULT NULL,
  PRIMARY KEY (`idAntecedenteTratamiento`),
  KEY `FK_antecedentestratamientos_idPaciente` (`idPaciente`),
  KEY `ntecedentestratamientosdTipoAntecedenteTratamiento` (`idTipoAntecedenteTratamiento`),
  CONSTRAINT `FK_antecedentestratamientos_idPaciente` FOREIGN KEY (`idPaciente`) REFERENCES `pacientes` (`idPaciente`),
  CONSTRAINT `ntecedentestratamientosdTipoAntecedenteTratamiento` FOREIGN KEY (`idTipoAntecedenteTratamiento`) REFERENCES `tiposantecedentestratamientos` (`idTipoAntecedenteTratamiento`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `archivos`
--

DROP TABLE IF EXISTS `archivos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `archivos` (
  `idArchivo` int(11) NOT NULL,
  `archivo` longblob,
  `fecha` datetime NOT NULL,
  `name` varchar(255) NOT NULL,
  `idNota` int(11) DEFAULT NULL,
  `idFacturaRecibida` int(11) DEFAULT NULL,
  PRIMARY KEY (`idArchivo`),
  KEY `FK_archivos_idFacturaRecibida` (`idFacturaRecibida`),
  KEY `FK_archivos_idNota` (`idNota`),
  CONSTRAINT `FK_archivos_idFacturaRecibida` FOREIGN KEY (`idFacturaRecibida`) REFERENCES `facturasrecibidas` (`idFacturaRecibida`),
  CONSTRAINT `FK_archivos_idNota` FOREIGN KEY (`idNota`) REFERENCES `notas` (`idNota`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bancos`
--

DROP TABLE IF EXISTS `bancos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bancos` (
  `idBanco` int(11) NOT NULL,
  `cuenta` varchar(255) DEFAULT NULL,
  `nombre` varchar(255) DEFAULT NULL,
  `entidad` varchar(255) DEFAULT NULL,
  `sucursal` varchar(255) DEFAULT NULL,
  `dc` varchar(255) DEFAULT NULL,
  `sufijo` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`idBanco`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `categoriasproductos`
--

DROP TABLE IF EXISTS `categoriasproductos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `categoriasproductos` (
  `idCategoriaProducto` int(11) NOT NULL,
  `categoriaProducto` varchar(255) NOT NULL,
  PRIMARY KEY (`idCategoriaProducto`),
  UNIQUE KEY `categoriaProducto` (`categoriaProducto`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `clientes`
--

DROP TABLE IF EXISTS `clientes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `clientes` (
  `idCliente` int(11) NOT NULL DEFAULT '0',
  `telefono2` varchar(255) DEFAULT NULL,
  `tiponacionalidadcliente` int(11) DEFAULT NULL,
  `web` varchar(255) DEFAULT NULL,
  `cuenta` varchar(255) DEFAULT NULL,
  `descuentoGlobal` double DEFAULT NULL,
  `sucursal` varchar(255) DEFAULT NULL,
  `idClienteContabildidad` varchar(255) DEFAULT NULL,
  `banco` varchar(255) DEFAULT NULL,
  `fechaBaja` date DEFAULT NULL,
  `codigoproveedor` varchar(255) DEFAULT NULL,
  `aperturado` tinyint(1) DEFAULT '0',
  `dg` varchar(255) DEFAULT NULL,
  `telefono1` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `fecharegistro` datetime NOT NULL,
  `recargo` tinyint(1) DEFAULT '0',
  `nombreComercial` varchar(255) DEFAULT NULL,
  `nombre` varchar(255) NOT NULL,
  `riesgomaximo` double DEFAULT NULL,
  `aceptado` tinyint(1) DEFAULT '0',
  `movil` varchar(255) DEFAULT NULL,
  `cifnif` varchar(255) DEFAULT NULL,
  `fax` varchar(255) DEFAULT NULL,
  `descuento` double DEFAULT NULL,
  `observaciones` mediumtext,
  `facturarEn` int(11) DEFAULT NULL,
  `idDivisa` int(11) DEFAULT NULL,
  `idDelegacion` int(11) DEFAULT NULL,
  `idDireccion` int(11) NOT NULL,
  `idEmpresaTransporte` int(11) DEFAULT NULL,
  `idActividad` int(11) DEFAULT NULL,
  `idComercial` int(11) DEFAULT NULL,
  `idSubcuenta431` int(11) DEFAULT NULL,
  `idDireccionPostal` int(11) DEFAULT NULL,
  `idSubcuenta430` int(11) DEFAULT NULL,
  `idFormaPago` int(11) DEFAULT NULL,
  `idNutricionista` int(11) DEFAULT NULL,
  `cancelado` tinyint(1) DEFAULT NULL,
  `idZona` int(11) DEFAULT NULL,
  `liquidacion` double(6,2) DEFAULT NULL,
  `kilometros` varchar(10) DEFAULT '',
  `tipoLiquidacion` int(11) DEFAULT NULL,
  PRIMARY KEY (`idCliente`),
  UNIQUE KEY `idDireccion` (`idDireccion`),
  KEY `FK_clientes_idDireccionPostal` (`idDireccionPostal`),
  KEY `FK_clientes_idDelegacion` (`idDelegacion`),
  KEY `FK_clientes_idDivisa` (`idDivisa`),
  KEY `FK_clientes_idComercial` (`idComercial`),
  KEY `FK_clientes_idFormaPago` (`idFormaPago`),
  KEY `FK_clientes_idActividad` (`idActividad`),
  KEY `FK_clientes_idZona` (`idZona`),
  CONSTRAINT `FK_clientes_idActividad` FOREIGN KEY (`idActividad`) REFERENCES `actividades` (`idActividad`),
  CONSTRAINT `FK_clientes_idComercial` FOREIGN KEY (`idComercial`) REFERENCES `comerciales` (`idComercial`),
  CONSTRAINT `FK_clientes_idDelegacion` FOREIGN KEY (`idDelegacion`) REFERENCES `delegaciones` (`idDelegacion`),
  CONSTRAINT `FK_clientes_idDireccion` FOREIGN KEY (`idDireccion`) REFERENCES `direcciones` (`idDireccion`),
  CONSTRAINT `FK_clientes_idDireccionPostal` FOREIGN KEY (`idDireccionPostal`) REFERENCES `direcciones` (`idDireccion`),
  CONSTRAINT `FK_clientes_idDivisa` FOREIGN KEY (`idDivisa`) REFERENCES `divisas` (`idDivisa`),
  CONSTRAINT `FK_clientes_idFormaPago` FOREIGN KEY (`idFormaPago`) REFERENCES `formapago` (`idFormaPago`),
  CONSTRAINT `FK_clientes_idZona` FOREIGN KEY (`idZona`) REFERENCES `zonas` (`idZona`),
  CONSTRAINT `FK_clientes_zona` FOREIGN KEY (`idZona`) REFERENCES `zonas` (`idZona`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `comerciales`
--

DROP TABLE IF EXISTS `comerciales`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `comerciales` (
  `idComercial` int(11) NOT NULL,
  `nombre` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `movil` varchar(255) DEFAULT NULL,
  `idDireccion` int(11) DEFAULT NULL,
  `idDelegacion` int(11) DEFAULT NULL,
  `baja` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`idComercial`),
  KEY `FK_comerciales_idDireccion` (`idDireccion`),
  KEY `FK_comerciales_idDelegacion` (`idDelegacion`),
  CONSTRAINT `FK_comerciales_idDelegacion` FOREIGN KEY (`idDelegacion`) REFERENCES `delegaciones` (`idDelegacion`),
  CONSTRAINT `FK_comerciales_idDireccion` FOREIGN KEY (`idDireccion`) REFERENCES `direcciones` (`idDireccion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `comerciales_clientes`
--

DROP TABLE IF EXISTS `comerciales_clientes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `comerciales_clientes` (
  `idCliente` int(11) NOT NULL DEFAULT '0',
  `idComercial` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`idCliente`,`idComercial`),
  KEY `FK_comerciales_clientes_idComercial` (`idComercial`),
  CONSTRAINT `FK_comerciales_clientes_idCliente` FOREIGN KEY (`idCliente`) REFERENCES `clientes` (`idCliente`),
  CONSTRAINT `FK_comerciales_clientes_idComercial` FOREIGN KEY (`idComercial`) REFERENCES `comerciales` (`idComercial`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `comisiones`
--

DROP TABLE IF EXISTS `comisiones`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `comisiones` (
  `idComision` int(11) NOT NULL,
  `importemaximo` double NOT NULL,
  `Comision` int(11) NOT NULL,
  `importeminimo` double NOT NULL,
  `idProducto` int(11) DEFAULT NULL,
  `idComercial` int(11) DEFAULT NULL,
  PRIMARY KEY (`idComision`),
  KEY `FK_comisiones_idComercial` (`idComercial`),
  KEY `FK_comisiones_idProducto` (`idProducto`),
  CONSTRAINT `FK_comisiones_idComercial` FOREIGN KEY (`idComercial`) REFERENCES `comerciales` (`idComercial`),
  CONSTRAINT `FK_comisiones_idProducto` FOREIGN KEY (`idProducto`) REFERENCES `productos` (`idProducto`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `comunidadesautonomas`
--

DROP TABLE IF EXISTS `comunidadesautonomas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `comunidadesautonomas` (
  `idComunidadAutonoma` int(11) NOT NULL,
  `comunidad` varchar(255) NOT NULL,
  PRIMARY KEY (`idComunidadAutonoma`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `delegaciones`
--

DROP TABLE IF EXISTS `delegaciones`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `delegaciones` (
  `idDelegacion` int(11) NOT NULL,
  `cif` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `telefono` varchar(255) DEFAULT NULL,
  `nombre` varchar(255) DEFAULT NULL,
  `idDireccion` int(11) NOT NULL,
  `cancelado` tinyint(1) NOT NULL,
  PRIMARY KEY (`idDelegacion`),
  UNIQUE KEY `idDireccion` (`idDireccion`),
  CONSTRAINT `FK_delegaciones_idDireccion` FOREIGN KEY (`idDireccion`) REFERENCES `direcciones` (`idDireccion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `direcciones`
--

DROP TABLE IF EXISTS `direcciones`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `direcciones` (
  `idDireccion` int(11) NOT NULL,
  `direccion` varchar(255) DEFAULT NULL,
  `nombre` varchar(255) DEFAULT NULL,
  `poblacion` varchar(255) DEFAULT NULL,
  `cp` varchar(255) DEFAULT NULL,
  `idPais` int(11) DEFAULT NULL,
  `idProvincia` int(11) DEFAULT NULL,
  PRIMARY KEY (`idDireccion`),
  KEY `FK_direcciones_idPais` (`idPais`),
  KEY `FK_direcciones_idProvincia` (`idProvincia`),
  CONSTRAINT `FK_direcciones_idPais` FOREIGN KEY (`idPais`) REFERENCES `paises` (`idPais`),
  CONSTRAINT `FK_direcciones_idProvincia` FOREIGN KEY (`idProvincia`) REFERENCES `provincias` (`idProvincia`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `divisas`
--

DROP TABLE IF EXISTS `divisas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `divisas` (
  `idDivisa` int(11) NOT NULL,
  `divisa` varchar(255) NOT NULL,
  `simbolo` varchar(255) NOT NULL,
  PRIMARY KEY (`idDivisa`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ejercicios`
--

DROP TABLE IF EXISTS `ejercicios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ejercicios` (
  `idEjercicio` int(11) NOT NULL,
  `fechaini` date NOT NULL,
  `ejercicio` varchar(255) NOT NULL,
  `fechaintermedia` date NOT NULL,
  `fechafin` date NOT NULL,
  PRIMARY KEY (`idEjercicio`),
  UNIQUE KEY `ejercicio` (`ejercicio`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `emails`
--

DROP TABLE IF EXISTS `emails`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `emails` (
  `idEmail` int(11) NOT NULL,
  `content` mediumtext,
  `idEmailOrigen` int(11) NOT NULL,
  `fecha` datetime NOT NULL,
  `from1` varchar(255) NOT NULL,
  `subject` varchar(255) DEFAULT NULL,
  `idUsuario` int(11) NOT NULL,
  PRIMARY KEY (`idEmail`),
  KEY `FK_emails_idUsuario` (`idUsuario`),
  CONSTRAINT `FK_emails_idUsuario` FOREIGN KEY (`idUsuario`) REFERENCES `usuarios` (`idUsuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `eventoscalendario`
--

DROP TABLE IF EXISTS `eventoscalendario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `eventoscalendario` (
  `idEventoCalendario` int(11) NOT NULL,
  `minutos` varchar(255) DEFAULT NULL,
  `titulo` mediumtext NOT NULL,
  `texto` mediumtext,
  `hora` varchar(255) DEFAULT NULL,
  `privado` tinyint(1) NOT NULL DEFAULT '0',
  `fechaintroduccion` datetime NOT NULL,
  `fecha` datetime NOT NULL,
  `idUsuario` int(11) NOT NULL,
  `idNutricionistaDestino` int(11) DEFAULT NULL,
  `idCliente` int(11) DEFAULT NULL,
  `fechaFin` datetime NOT NULL,
  `cancelado` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`idEventoCalendario`),
  KEY `FK_eventoscalendario_idUsuario` (`idUsuario`),
  KEY `FK_eventoscalendario_idNutricionistaDestino` (`idNutricionistaDestino`),
  KEY `FK_eventoscalendario_idCliente` (`idCliente`),
  CONSTRAINT `FK_eventoscalendario_idCliente` FOREIGN KEY (`idCliente`) REFERENCES `clientes` (`idCliente`),
  CONSTRAINT `FK_eventoscalendario_idNutricionistaDestino` FOREIGN KEY (`idNutricionistaDestino`) REFERENCES `nutricionistas` (`idNutricionista`),
  CONSTRAINT `FK_eventoscalendario_idUsuario` FOREIGN KEY (`idUsuario`) REFERENCES `usuarios` (`idUsuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `factura`
--

DROP TABLE IF EXISTS `factura`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `factura` (
  `idFactura` int(11) NOT NULL,
  `contabilidad` tinyint(1) DEFAULT '0',
  `recargo` tinyint(1) NOT NULL DEFAULT '0',
  `isFacturaVentaDirecta` tinyint(1) NOT NULL DEFAULT '0',
  `tipoFacturaFacturaEmitidaAsiento` int(11) DEFAULT NULL,
  `pagado` tinyint(1) DEFAULT '0',
  `fechafactura` date DEFAULT NULL,
  `validada` tinyint(1) DEFAULT '0',
  `comentarios` varchar(255) DEFAULT NULL,
  `idNumeroFactura` int(11) NOT NULL,
  `idCliente` int(11) NOT NULL,
  `idFormaPago` int(11) DEFAULT NULL,
  PRIMARY KEY (`idFactura`),
  KEY `FK_factura_idCliente` (`idCliente`),
  KEY `FK_factura_idFormaPago` (`idFormaPago`),
  KEY `FK_factura_idNumeroFactura` (`idNumeroFactura`),
  CONSTRAINT `FK_factura_idCliente` FOREIGN KEY (`idCliente`) REFERENCES `clientes` (`idCliente`),
  CONSTRAINT `FK_factura_idFormaPago` FOREIGN KEY (`idFormaPago`) REFERENCES `formapago` (`idFormaPago`),
  CONSTRAINT `FK_factura_idNumeroFactura` FOREIGN KEY (`idNumeroFactura`) REFERENCES `numerosfacturas` (`idNumeroFactura`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `formapago`
--

DROP TABLE IF EXISTS `formapago`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `formapago` (
  `idFormaPago` int(11) NOT NULL,
  `diasPrimerPago` int(11) DEFAULT NULL,
  `numeroPagos` int(11) DEFAULT NULL,
  `diasEntrePagos` int(11) DEFAULT NULL,
  `pago` varchar(255) DEFAULT NULL,
  `diaFijoPago` int(11) DEFAULT NULL,
  `tipoFormaPago` int(11) DEFAULT NULL,
  PRIMARY KEY (`idFormaPago`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `iva`
--

DROP TABLE IF EXISTS `iva`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `iva` (
  `idIva` int(11) NOT NULL,
  `recargo` float DEFAULT NULL,
  `valor` int(11) NOT NULL,
  `tipo` varchar(255) NOT NULL,
  `bloqueado` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`idIva`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `keygenerator`
--

DROP TABLE IF EXISTS `keygenerator`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `keygenerator` (
  `genkey` varchar(50) NOT NULL,
  `genvalue` decimal(38,0) DEFAULT NULL,
  PRIMARY KEY (`genkey`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `logusuarios`
--

DROP TABLE IF EXISTS `logusuarios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `logusuarios` (
  `idLogUsuario` int(11) NOT NULL,
  `fecha` datetime NOT NULL,
  `idUsuario` int(11) NOT NULL,
  `codigoSesion` varchar(100) NOT NULL,
  `traza` longtext NOT NULL,
  `ultimaAccion` datetime NOT NULL,
  `ip` varchar(45) NOT NULL,
  PRIMARY KEY (`idLogUsuario`),
  KEY `FK_logusuarios_idUsuario` (`idUsuario`),
  CONSTRAINT `FK_logusuarios_idUsuario` FOREIGN KEY (`idUsuario`) REFERENCES `usuarios` (`idUsuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `medicionesespecificas`
--

DROP TABLE IF EXISTS `medicionesespecificas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `medicionesespecificas` (
  `idMedicionEspecifica` int(11) NOT NULL,
  `poMuscMin` varchar(255) NOT NULL,
  `poMuscMax` varchar(255) NOT NULL,
  `peso` varchar(255) NOT NULL,
  `grasa` varchar(255) NOT NULL,
  `porcentajeGrasa` varchar(255) NOT NULL,
  `grasaIdMin` varchar(255) NOT NULL,
  `mbi` varchar(255) NOT NULL,
  `grasaIdMax` varchar(255) NOT NULL,
  `musculo` varchar(255) NOT NULL,
  `grasaVisc` varchar(255) NOT NULL,
  `musculoIdMax` varchar(255) NOT NULL,
  `masaOsea` varchar(255) NOT NULL,
  `porcentajeAgua` varchar(255) NOT NULL,
  `edadMet` varchar(255) NOT NULL,
  `litrosAgua` varchar(255) NOT NULL,
  `fecha` date NOT NULL,
  `aguaId` varchar(255) NOT NULL,
  `musculoIdMin` varchar(255) NOT NULL,
  `retencionLiquidos` varchar(255) NOT NULL,
  `metabolismoBasal` varchar(255) NOT NULL,
  `idPaciente` int(11) NOT NULL,
  PRIMARY KEY (`idMedicionEspecifica`),
  KEY `FK_medicionesespecificas_idPaciente` (`idPaciente`),
  CONSTRAINT `FK_medicionesespecificas_idPaciente` FOREIGN KEY (`idPaciente`) REFERENCES `pacientes` (`idPaciente`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `medicionesgenerales`
--

DROP TABLE IF EXISTS `medicionesgenerales`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `medicionesgenerales` (
  `idMedicionGeneral` int(11) NOT NULL,
  `cintura` varchar(255) NOT NULL,
  `cadera` varchar(255) NOT NULL,
  `peso` varchar(255) NOT NULL,
  `icc` varchar(255) NOT NULL,
  `brazo` varchar(255) NOT NULL,
  `imc` varchar(255) NOT NULL,
  `muslo` varchar(255) NOT NULL,
  `porcentajeGrasa` varchar(255) NOT NULL,
  `tenmin` varchar(255) NOT NULL,
  `fecha` date DEFAULT NULL,
  `tenmax` varchar(255) NOT NULL,
  `pesoIdeal` varchar(255) NOT NULL,
  `idPaciente` int(11) NOT NULL,
  PRIMARY KEY (`idMedicionGeneral`),
  KEY `FK_medicionesgenerales_idPaciente` (`idPaciente`),
  CONSTRAINT `FK_medicionesgenerales_idPaciente` FOREIGN KEY (`idPaciente`) REFERENCES `pacientes` (`idPaciente`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `medicionessegmentales`
--

DROP TABLE IF EXISTS `medicionessegmentales`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `medicionessegmentales` (
  `idMedicionSegmental` int(11) NOT NULL,
  `pdGrasa` varchar(255) NOT NULL,
  `fecha` date NOT NULL,
  `pdMusculo` varchar(255) NOT NULL,
  `bdGrasa` varchar(255) NOT NULL,
  `piGrasa` varchar(255) NOT NULL,
  `biGrasa` varchar(255) NOT NULL,
  `piMusculo` varchar(255) NOT NULL,
  `tGrasa` varchar(255) NOT NULL,
  `biMusculo` varchar(255) NOT NULL,
  `tMusculo` varchar(255) NOT NULL,
  `bdMusculo` varchar(255) NOT NULL,
  `idPaciente` int(11) NOT NULL,
  PRIMARY KEY (`idMedicionSegmental`),
  KEY `FK_medicionessegmentales_idPaciente` (`idPaciente`),
  CONSTRAINT `FK_medicionessegmentales_idPaciente` FOREIGN KEY (`idPaciente`) REFERENCES `pacientes` (`idPaciente`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `medidas`
--

DROP TABLE IF EXISTS `medidas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `medidas` (
  `idMedida` int(11) NOT NULL,
  `acronimo` varchar(255) NOT NULL,
  `medida` varchar(255) NOT NULL,
  PRIMARY KEY (`idMedida`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `notas`
--

DROP TABLE IF EXISTS `notas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `notas` (
  `idNota` int(11) NOT NULL,
  `fechaintroduccion` datetime NOT NULL,
  `titulo` mediumtext NOT NULL,
  `numeroArchivos` int(11) NOT NULL,
  `tipo` int(11) DEFAULT NULL,
  `sentido` int(11) DEFAULT NULL,
  `fechaalarma` date DEFAULT NULL,
  `textoalarma` mediumtext,
  `nota` mediumtext,
  `recordatorio` int(11) NOT NULL,
  `fecha` datetime NOT NULL,
  `idUsuario` int(11) NOT NULL,
  `idCliente` int(11) NOT NULL,
  PRIMARY KEY (`idNota`),
  KEY `FK_notas_idUsuario` (`idUsuario`),
  KEY `FK_notas_idCliente` (`idCliente`),
  CONSTRAINT `FK_notas_idCliente` FOREIGN KEY (`idCliente`) REFERENCES `clientes` (`idCliente`),
  CONSTRAINT `FK_notas_idUsuario` FOREIGN KEY (`idUsuario`) REFERENCES `usuarios` (`idUsuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `numerosalbaranes`
--

DROP TABLE IF EXISTS `numerosalbaranes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `numerosalbaranes` (
  `idNumeroAlbaran` int(11) NOT NULL,
  `numeroAlbaran` int(11) NOT NULL,
  `idEjercicio` int(11) NOT NULL,
  PRIMARY KEY (`idNumeroAlbaran`),
  UNIQUE KEY `UNQ_numerosalbaranes_0` (`numeroAlbaran`,`idEjercicio`),
  KEY `FK_numerosalbaranes_idEjercicio` (`idEjercicio`),
  CONSTRAINT `FK_numerosalbaranes_idEjercicio` FOREIGN KEY (`idEjercicio`) REFERENCES `ejercicios` (`idEjercicio`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `numerosfacturas`
--

DROP TABLE IF EXISTS `numerosfacturas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `numerosfacturas` (
  `idNumeroFactura` int(11) NOT NULL,
  `numeroFactura` int(11) NOT NULL,
  `fecha` date NOT NULL,
  `idSerieFactura` int(11) NOT NULL,
  `idEjercicio` int(11) NOT NULL,
  PRIMARY KEY (`idNumeroFactura`),
  UNIQUE KEY `UNQ_numerosfacturas_0` (`numeroFactura`,`idEjercicio`,`idSerieFactura`),
  KEY `FK_numerosfacturas_idEjercicio` (`idEjercicio`),
  KEY `FK_numerosfacturas_idSerieFactura` (`idSerieFactura`),
  CONSTRAINT `FK_numerosfacturas_idEjercicio` FOREIGN KEY (`idEjercicio`) REFERENCES `ejercicios` (`idEjercicio`),
  CONSTRAINT `FK_numerosfacturas_idSerieFactura` FOREIGN KEY (`idSerieFactura`) REFERENCES `seriesfacturas` (`idSerieFactura`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `numerospedidos`
--

DROP TABLE IF EXISTS `numerospedidos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `numerospedidos` (
  `idNumeroPedido` int(11) NOT NULL,
  `numeroPedido` int(11) NOT NULL,
  `idEjercicio` int(11) NOT NULL,
  PRIMARY KEY (`idNumeroPedido`),
  UNIQUE KEY `UNQ_numerospedidos_0` (`numeroPedido`,`idEjercicio`),
  KEY `FK_numerospedidos_idEjercicio` (`idEjercicio`),
  CONSTRAINT `FK_numerospedidos_idEjercicio` FOREIGN KEY (`idEjercicio`) REFERENCES `ejercicios` (`idEjercicio`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `nutricionistas`
--

DROP TABLE IF EXISTS `nutricionistas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `nutricionistas` (
  `idNutricionista` int(11) NOT NULL,
  `telefonoEmpresa` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `telefonoPersonal` varchar(255) DEFAULT NULL,
  `nombre` varchar(255) DEFAULT NULL,
  `cif` varchar(255) DEFAULT NULL,
  `idDelegacion` int(11) DEFAULT NULL,
  `idDireccion` int(11) NOT NULL,
  `jefe` int(11) DEFAULT NULL,
  `idNutricionistaJefe` int(11) DEFAULT NULL,
  `cancelado` tinyint(1) DEFAULT NULL,
  `referencia` varchar(45) DEFAULT NULL,
  `horas` int(11) DEFAULT '0',
  PRIMARY KEY (`idNutricionista`),
  UNIQUE KEY `idDireccion` (`idDireccion`),
  KEY `FK_nutricionistas_idDelegacion` (`idDelegacion`),
  CONSTRAINT `FK_nutricionistas_idDelegacion` FOREIGN KEY (`idDelegacion`) REFERENCES `delegaciones` (`idDelegacion`),
  CONSTRAINT `FK_nutricionistas_idDireccion` FOREIGN KEY (`idDireccion`) REFERENCES `direcciones` (`idDireccion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `nutricionistas_clientes`
--

DROP TABLE IF EXISTS `nutricionistas_clientes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `nutricionistas_clientes` (
  `idCliente` int(11) NOT NULL DEFAULT '0',
  `idNutricionista` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`idCliente`,`idNutricionista`),
  KEY `FK_nutricionistas_clientes_idNutricionista` (`idNutricionista`),
  CONSTRAINT `FK_nutricionistas_clientes_idCliente` FOREIGN KEY (`idCliente`) REFERENCES `clientes` (`idCliente`),
  CONSTRAINT `FK_nutricionistas_clientes_idNutricionista` FOREIGN KEY (`idNutricionista`) REFERENCES `nutricionistas` (`idNutricionista`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pacientes`
--

DROP TABLE IF EXISTS `pacientes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pacientes` (
  `idPaciente` int(11) NOT NULL,
  `edad` int(11) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `fechaInicioTratamiento` date DEFAULT NULL,
  `altura` varchar(255) DEFAULT NULL,
  `sexo` tinyint(1) DEFAULT '0',
  `telefono` varchar(255) DEFAULT NULL,
  `historial` mediumtext,
  `fechaNacimiento` date DEFAULT NULL,
  `nombre` varchar(255) DEFAULT NULL,
  `idCliente` int(11) DEFAULT NULL,
  `idNutricionista` int(11) DEFAULT NULL,
  `idDireccion` int(11) DEFAULT NULL,
  `idTipoConsulta` int(11) DEFAULT NULL,
  `codigoQp` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`idPaciente`),
  UNIQUE KEY `idDireccion` (`idDireccion`),
  KEY `FK_pacientes_idTipoConsulta` (`idTipoConsulta`),
  KEY `FK_pacientes_idNutricionista` (`idNutricionista`),
  KEY `FK_pacientes_idCliente` (`idCliente`),
  CONSTRAINT `FK_pacientes_idCliente` FOREIGN KEY (`idCliente`) REFERENCES `clientes` (`idCliente`),
  CONSTRAINT `FK_pacientes_idDireccion` FOREIGN KEY (`idDireccion`) REFERENCES `direcciones` (`idDireccion`),
  CONSTRAINT `FK_pacientes_idNutricionista` FOREIGN KEY (`idNutricionista`) REFERENCES `nutricionistas` (`idNutricionista`),
  CONSTRAINT `FK_pacientes_idTipoConsulta` FOREIGN KEY (`idTipoConsulta`) REFERENCES `tipoconsultas` (`idTipoConsulta`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pago`
--

DROP TABLE IF EXISTS `pago`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pago` (
  `idPago` int(11) NOT NULL,
  `idNutri` int(11) NOT NULL,
  `nombreNutri` varchar(100) DEFAULT NULL,
  `dniNutri` varchar(12) DEFAULT NULL,
  `cantidad` double DEFAULT NULL,
  `periodo` varchar(100) DEFAULT NULL,
  `provincia` varchar(25) DEFAULT NULL,
  `gastos` double DEFAULT NULL,
  `fechaCreacion` datetime DEFAULT NULL,
  `fechaConfirmacion` datetime DEFAULT NULL,
  `comprobado` tinyint(1) DEFAULT NULL,
  `observaciones` varchar(255) DEFAULT '',
  `cancelado` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`idPago`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `paises`
--

DROP TABLE IF EXISTS `paises`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `paises` (
  `idPais` int(11) NOT NULL,
  `pais` varchar(255) NOT NULL,
  `paisPrincipal` int(11) NOT NULL,
  PRIMARY KEY (`idPais`),
  UNIQUE KEY `pais` (`pais`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pedidoproductos`
--

DROP TABLE IF EXISTS `pedidoproductos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pedidoproductos` (
  `idPedidoProductos` int(11) NOT NULL,
  `precio` double DEFAULT NULL,
  `base` double DEFAULT NULL,
  `unidadescargadas` double NOT NULL,
  `recargo` double DEFAULT NULL,
  `descontadoAlmacen` tinyint(1) NOT NULL DEFAULT '0',
  `observaciones` mediumtext,
  `numeroAgrupacion` int(11) DEFAULT NULL,
  `fechacaducidad` time DEFAULT NULL,
  `descuento` double DEFAULT NULL,
  `total` double DEFAULT NULL,
  `descuentoGlobal` double DEFAULT NULL,
  `iva` double DEFAULT NULL,
  `unidades` double DEFAULT NULL,
  `idPedido` int(11) DEFAULT NULL,
  `idSesion` int(11) DEFAULT NULL,
  `bonificados` double NOT NULL DEFAULT '0',
  `idIva` int(11) NOT NULL,
  `idProducto` int(11) DEFAULT NULL,
  PRIMARY KEY (`idPedidoProductos`),
  KEY `FK_pedidoproductos_idPedido` (`idPedido`),
  KEY `FK_pedidoproductos_idProducto` (`idProducto`),
  KEY `FK_pedidoproductos_idIva` (`idIva`),
  KEY `FK_pedidoproductos_idSesion` (`idSesion`),
  CONSTRAINT `FK_pedidoproductos_idIva` FOREIGN KEY (`idIva`) REFERENCES `iva` (`idIva`),
  CONSTRAINT `FK_pedidoproductos_idPedido` FOREIGN KEY (`idPedido`) REFERENCES `pedidos` (`idPedido`),
  CONSTRAINT `FK_pedidoproductos_idProducto` FOREIGN KEY (`idProducto`) REFERENCES `productos` (`idProducto`),
  CONSTRAINT `FK_pedidoproductos_idSesion` FOREIGN KEY (`idSesion`) REFERENCES `sesiones` (`idSesion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pedidos`
--

DROP TABLE IF EXISTS `pedidos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pedidos` (
  `idPedido` int(11) NOT NULL,
  `pedidoWeb` tinyint(1) DEFAULT '0',
  `numeroAlbaran` int(11) DEFAULT NULL,
  `sf` tinyint(1) DEFAULT '0',
  `descuento` double DEFAULT NULL,
  `telefono` varchar(255) DEFAULT NULL,
  `observaciones` mediumtext,
  `tipoEnvio` int(11) DEFAULT NULL,
  `codigoEdi` varchar(255) DEFAULT NULL,
  `valorDivisa` double DEFAULT NULL,
  `cobrado` tinyint(1) NOT NULL DEFAULT '0',
  `horaEntrega` varchar(255) DEFAULT NULL,
  `crearmodificarpedido` int(11) NOT NULL,
  `minsEntrega` varchar(255) DEFAULT NULL,
  `facturado` tinyint(1) NOT NULL DEFAULT '0',
  `muelle` varchar(255) DEFAULT NULL,
  `fechaentregado` datetime DEFAULT NULL,
  `nPalets` varchar(255) DEFAULT NULL,
  `fechaSecundaria` datetime DEFAULT NULL,
  `nPaquetes` varchar(255) DEFAULT NULL,
  `fechaSms` date DEFAULT NULL,
  `peso` varchar(255) DEFAULT NULL,
  `observacionesdescuento` mediumtext,
  `horaTransporteSecundaria` varchar(255) DEFAULT NULL,
  `ordenentransporte` int(11) NOT NULL,
  `fechaEntregaInicial` date DEFAULT NULL,
  `movil` varchar(255) DEFAULT NULL,
  `fechaenviado` datetime DEFAULT NULL,
  `cancelado` tinyint(1) NOT NULL DEFAULT '0',
  `fechapedido` datetime NOT NULL,
  `estado` int(11) NOT NULL,
  `horaSms` varchar(255) DEFAULT NULL,
  `contacto` varchar(255) DEFAULT NULL,
  `observacionesFabricacion` mediumtext,
  `fechaFactura` date DEFAULT NULL,
  `idFactura` int(11) DEFAULT NULL,
  `idCliente` int(11) NOT NULL,
  `idDireccion` int(11) DEFAULT NULL,
  `idNumeroPedido` int(11) NOT NULL,
  `idAlbaran` int(11) DEFAULT NULL,
  `idNutricionista` int(11) DEFAULT NULL,
  `idVisita` int(11) DEFAULT NULL,
  `idComercial` int(11) DEFAULT NULL,
  `idDivisa` int(11) DEFAULT NULL,
  `codigoQpnut` varchar(45) DEFAULT NULL,
  `tipoPedido` varchar(45) DEFAULT NULL,
  `fechaConfirmado` date DEFAULT NULL,
  `idEventoCalendario` int(11) DEFAULT NULL,
  `comprobado` tinyint(1) NOT NULL DEFAULT '0',
  `seguimiento` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`idPedido`),
  KEY `FK_pedidos_idComercial` (`idComercial`),
  KEY `FK_pedidos_idAlbaran` (`idAlbaran`),
  KEY `FK_pedidos_idCliente` (`idCliente`),
  KEY `FK_pedidos_idNumeroPedido` (`idNumeroPedido`),
  KEY `FK_pedidos_idDivisa` (`idDivisa`),
  KEY `FK_pedidos_idNutricionista` (`idNutricionista`),
  KEY `FK_pedidos_idDireccion` (`idDireccion`),
  KEY `FK_pedidos_idFactura` (`idFactura`),
  KEY `FK_pedidos_idVisita` (`idVisita`),
  KEY `FK_pedidos_Evento` (`idEventoCalendario`),
  CONSTRAINT `FK_pedidos_Evento` FOREIGN KEY (`idEventoCalendario`) REFERENCES `eventoscalendario` (`idEventoCalendario`),
  CONSTRAINT `FK_pedidos_idAlbaran` FOREIGN KEY (`idAlbaran`) REFERENCES `albaranes` (`idAlbaran`),
  CONSTRAINT `FK_pedidos_idCliente` FOREIGN KEY (`idCliente`) REFERENCES `clientes` (`idCliente`),
  CONSTRAINT `FK_pedidos_idComercial` FOREIGN KEY (`idComercial`) REFERENCES `comerciales` (`idComercial`),
  CONSTRAINT `FK_pedidos_idDireccion` FOREIGN KEY (`idDireccion`) REFERENCES `direcciones` (`idDireccion`),
  CONSTRAINT `FK_pedidos_idDivisa` FOREIGN KEY (`idDivisa`) REFERENCES `divisas` (`idDivisa`),
  CONSTRAINT `FK_pedidos_idFactura` FOREIGN KEY (`idFactura`) REFERENCES `factura` (`idFactura`),
  CONSTRAINT `FK_pedidos_idNumeroPedido` FOREIGN KEY (`idNumeroPedido`) REFERENCES `numerospedidos` (`idNumeroPedido`),
  CONSTRAINT `FK_pedidos_idNutricionista` FOREIGN KEY (`idNutricionista`) REFERENCES `nutricionistas` (`idNutricionista`),
  CONSTRAINT `FK_pedidos_idVisita` FOREIGN KEY (`idVisita`) REFERENCES `visitas` (`idVisita`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `personacontacto`
--

DROP TABLE IF EXISTS `personacontacto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `personacontacto` (
  `idPersonaContacto` int(11) NOT NULL,
  `observaciones` varchar(255) DEFAULT NULL,
  `principal` tinyint(1) DEFAULT '0',
  `telefono1` varchar(255) DEFAULT NULL,
  `nombre` varchar(255) NOT NULL,
  `cargo` varchar(255) DEFAULT NULL,
  `movil` varchar(255) DEFAULT NULL,
  `fax` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `idProveedor` int(11) DEFAULT NULL,
  `idCliente` int(11) DEFAULT NULL,
  PRIMARY KEY (`idPersonaContacto`),
  KEY `FK_personacontacto_idProveedor` (`idProveedor`),
  KEY `FK_personacontacto_idCliente` (`idCliente`),
  CONSTRAINT `FK_personacontacto_idCliente` FOREIGN KEY (`idCliente`) REFERENCES `clientes` (`idCliente`),
  CONSTRAINT `FK_personacontacto_idProveedor` FOREIGN KEY (`idProveedor`) REFERENCES `proveedores` (`idProveedor`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `productos`
--

DROP TABLE IF EXISTS `productos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `productos` (
  `idProducto` int(11) NOT NULL,
  `visibleOrdenesFabricacion` tinyint(1) NOT NULL DEFAULT '0',
  `acronimo` varchar(255) DEFAULT NULL,
  `alto` double NOT NULL,
  `unidadesproducto` int(11) NOT NULL,
  `ancho` double NOT NULL,
  `pesobrutoenkg` double NOT NULL,
  `largo` double NOT NULL,
  `ean14` varchar(255) DEFAULT NULL,
  `codigoTaric` varchar(255) NOT NULL,
  `pesonetoenkg` double NOT NULL,
  `tieneMedidasCaja` tinyint(1) NOT NULL DEFAULT '0',
  `productoIngles` varchar(255) NOT NULL,
  `ean13` varchar(255) DEFAULT NULL,
  `precio` double NOT NULL,
  `referencia` varchar(255) NOT NULL,
  `producto` varchar(255) NOT NULL,
  `visibleAlbaranes` tinyint(1) NOT NULL DEFAULT '0',
  `idIva` int(11) NOT NULL,
  `idCategoriaProducto` int(11) NOT NULL,
  `idMedida` int(11) DEFAULT NULL,
  `precioPVP` double DEFAULT NULL,
  `orden` int(11) DEFAULT NULL,
  PRIMARY KEY (`idProducto`),
  KEY `FK_productos_idIva` (`idIva`),
  KEY `FK_productos_idMedida` (`idMedida`),
  KEY `FK_productos_idCategoriaProducto` (`idCategoriaProducto`),
  CONSTRAINT `FK_productos_idCategoriaProducto` FOREIGN KEY (`idCategoriaProducto`) REFERENCES `categoriasproductos` (`idCategoriaProducto`),
  CONSTRAINT `FK_productos_idIva` FOREIGN KEY (`idIva`) REFERENCES `iva` (`idIva`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `productosdelegaciones`
--

DROP TABLE IF EXISTS `productosdelegaciones`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `productosdelegaciones` (
  `idProductoDelegacion` int(11) NOT NULL,
  `stock` double NOT NULL,
  `idDelegacion` int(11) DEFAULT NULL,
  `idProducto` int(11) DEFAULT NULL,
  PRIMARY KEY (`idProductoDelegacion`),
  UNIQUE KEY `UNQ_productosdelegaciones_0` (`idDelegacion`,`idProducto`),
  KEY `FK_productosdelegaciones_idProducto` (`idProducto`),
  CONSTRAINT `FK_productosdelegaciones_idDelegacion` FOREIGN KEY (`idDelegacion`) REFERENCES `delegaciones` (`idDelegacion`),
  CONSTRAINT `FK_productosdelegaciones_idProducto` FOREIGN KEY (`idProducto`) REFERENCES `productos` (`idProducto`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `provincias`
--

DROP TABLE IF EXISTS `provincias`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `provincias` (
  `idProvincia` int(11) NOT NULL,
  `codigoProvincia` varchar(255) DEFAULT NULL,
  `provincia` varchar(255) NOT NULL,
  `idPais` int(11) NOT NULL,
  `idComunidadAutonoma` int(11) DEFAULT NULL,
  PRIMARY KEY (`idProvincia`),
  KEY `FK_provincias_idPais` (`idPais`),
  KEY `FK_provincias_idComunidadAutonoma` (`idComunidadAutonoma`),
  CONSTRAINT `FK_provincias_idComunidadAutonoma` FOREIGN KEY (`idComunidadAutonoma`) REFERENCES `comunidadesautonomas` (`idComunidadAutonoma`),
  CONSTRAINT `FK_provincias_idPais` FOREIGN KEY (`idPais`) REFERENCES `paises` (`idPais`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `recursos`
--

DROP TABLE IF EXISTS `recursos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `recursos` (
  `idRecurso` int(11) NOT NULL,
  `direccionLinea2` varchar(255) NOT NULL,
  `logo` varchar(255) NOT NULL,
  `bancoNombre` varchar(255) NOT NULL,
  `fax` varchar(255) NOT NULL,
  `bancoDireccion` varchar(255) NOT NULL,
  `telefono2` varchar(255) NOT NULL,
  `bancoNumero` varchar(255) NOT NULL,
  `poblacion` varchar(255) NOT NULL,
  `bancoIban` varchar(255) NOT NULL,
  `mostrarCheckBoxLogo` tinyint(1) NOT NULL DEFAULT '0',
  `bancoSwift` varchar(255) NOT NULL,
  `mostrarResumen` tinyint(1) NOT NULL DEFAULT '0',
  `letra` varchar(255) NOT NULL,
  `cif` varchar(255) NOT NULL,
  `diaInicio` int(11) NOT NULL,
  `version` varchar(255) DEFAULT NULL,
  `mesInicio` int(11) NOT NULL,
  `telefono1` varchar(255) NOT NULL,
  `diaFin` int(11) NOT NULL,
  `provincia` varchar(255) NOT NULL,
  `mesFin` int(11) NOT NULL,
  `empresa` varchar(255) NOT NULL,
  `tablecolorHeaderR` int(11) NOT NULL,
  `email` varchar(255) NOT NULL,
  `tablecolorHeaderG` int(11) NOT NULL,
  `mostrarContabilidad` tinyint(1) NOT NULL DEFAULT '0',
  `tablecolorHeaderB` int(11) NOT NULL,
  `cp` varchar(255) NOT NULL,
  `opComercial` int(11) NOT NULL,
  `direccionLinea1` varchar(255) NOT NULL,
  `estrictoDescontarAlmacen` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`idRecurso`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `seriesfacturas`
--

DROP TABLE IF EXISTS `seriesfacturas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `seriesfacturas` (
  `idSerieFactura` int(11) NOT NULL,
  `serieFactura` varchar(1) NOT NULL,
  `serieFacturaPrincipal` int(11) NOT NULL,
  PRIMARY KEY (`idSerieFactura`),
  UNIQUE KEY `serieFactura` (`serieFactura`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sesiones`
--

DROP TABLE IF EXISTS `sesiones`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sesiones` (
  `idSesion` int(11) NOT NULL,
  `codigoSesion` varchar(255) NOT NULL,
  `fecha` datetime NOT NULL,
  `ip` varchar(255) NOT NULL,
  `idUsuario` int(11) DEFAULT NULL,
  PRIMARY KEY (`idSesion`),
  KEY `FK_sesiones_idUsuario` (`idUsuario`),
  CONSTRAINT `FK_sesiones_idUsuario` FOREIGN KEY (`idUsuario`) REFERENCES `usuarios` (`idUsuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tipoabonos`
--

DROP TABLE IF EXISTS `tipoabonos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tipoabonos` (
  `idTipoAbono` int(11) NOT NULL,
  `tipoAbono` varchar(255) NOT NULL,
  `idIva` int(11) NOT NULL,
  `idSubcuenta` int(11) DEFAULT NULL,
  PRIMARY KEY (`idTipoAbono`),
  UNIQUE KEY `tipoAbono` (`tipoAbono`),
  KEY `FK_tipoabonos_idSubcuenta` (`idSubcuenta`),
  KEY `FK_tipoabonos_idIva` (`idIva`),
  CONSTRAINT `FK_tipoabonos_idIva` FOREIGN KEY (`idIva`) REFERENCES `iva` (`idIva`),
  CONSTRAINT `FK_tipoabonos_idSubcuenta` FOREIGN KEY (`idSubcuenta`) REFERENCES `subcuentas` (`idSubcuenta`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tipoconsultas`
--

DROP TABLE IF EXISTS `tipoconsultas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tipoconsultas` (
  `idTipoConsulta` int(11) NOT NULL,
  `tipoConsulta` varchar(255) NOT NULL,
  PRIMARY KEY (`idTipoConsulta`),
  UNIQUE KEY `tipoConsulta` (`tipoConsulta`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tipopedidoes`
--

DROP TABLE IF EXISTS `tipopedidoes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tipopedidoes` (
  `idTipoPedido` int(11) NOT NULL,
  `tipo` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`idTipoPedido`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tiposantecedentesclinicos`
--

DROP TABLE IF EXISTS `tiposantecedentesclinicos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tiposantecedentesclinicos` (
  `idTipoAntecedenteClinico` int(11) NOT NULL,
  `tipoAntecedenteClinico` varchar(255) NOT NULL,
  `idPaciente` int(11) DEFAULT NULL,
  PRIMARY KEY (`idTipoAntecedenteClinico`),
  UNIQUE KEY `tipoAntecedenteClinico` (`tipoAntecedenteClinico`),
  KEY `FK_tiposantecedentesclinicos_1` (`idPaciente`),
  CONSTRAINT `FK_tiposantecedentesclinicos_1` FOREIGN KEY (`idPaciente`) REFERENCES `pacientes` (`idPaciente`),
  CONSTRAINT `FK_tiposantecedentesclinicos_idPaciente` FOREIGN KEY (`idPaciente`) REFERENCES `pacientes` (`idPaciente`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tiposantecedentestratamientos`
--

DROP TABLE IF EXISTS `tiposantecedentestratamientos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tiposantecedentestratamientos` (
  `idTipoAntecedenteTratamiento` int(11) NOT NULL,
  `tipoAntecedenteTratamiento` varchar(255) NOT NULL,
  `idPaciente` int(11) NOT NULL,
  PRIMARY KEY (`idTipoAntecedenteTratamiento`),
  UNIQUE KEY `tipoAntecedenteTratamiento` (`tipoAntecedenteTratamiento`),
  KEY `FK_tiposantecedentestratamientos_1` (`idPaciente`),
  CONSTRAINT `FK_tiposantecedentestratamientos_1` FOREIGN KEY (`idPaciente`) REFERENCES `pacientes` (`idPaciente`),
  CONSTRAINT `FK_tiposantecedentestratamientos_idPaciente` FOREIGN KEY (`idPaciente`) REFERENCES `pacientes` (`idPaciente`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `usuarios`
--

DROP TABLE IF EXISTS `usuarios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `usuarios` (
  `idUsuario` int(11) NOT NULL,
  `web` tinyint(1) DEFAULT '0',
  `password` blob NOT NULL,
  `clientes` tinyint(1) DEFAULT '0',
  `smtpServerPort` varchar(255) DEFAULT NULL,
  `contabilidad` tinyint(1) DEFAULT '0',
  `smtpServerPassword` varchar(255) DEFAULT NULL,
  `firma` mediumtext,
  `smtpServerAuth` tinyint(1) NOT NULL DEFAULT '0',
  `usuario` varchar(255) NOT NULL,
  `activado` tinyint(1) NOT NULL DEFAULT '0',
  `tipo` int(11) NOT NULL,
  `resumen` tinyint(1) DEFAULT '0',
  `fax` varchar(255) DEFAULT NULL,
  `proveedores` tinyint(1) DEFAULT '0',
  `smtpServer` varchar(255) DEFAULT NULL,
  `ventas` tinyint(1) DEFAULT '0',
  `smtpServerEmail` varchar(255) DEFAULT NULL,
  `almacen` tinyint(1) DEFAULT '0',
  `email` varchar(255) DEFAULT NULL,
  `facturas` tinyint(1) DEFAULT '0',
  `movil` varchar(255) DEFAULT NULL,
  `informes` tinyint(1) DEFAULT '0',
  `smtpServerSSL` tinyint(1) NOT NULL DEFAULT '0',
  `smtpServerUser` varchar(255) DEFAULT NULL,
  `nombre` varchar(255) DEFAULT NULL,
  `idNutricionista` int(11) DEFAULT NULL,
  `idDelegacion` int(11) DEFAULT NULL,
  `idComercial` int(11) DEFAULT NULL,
  `idDireccion` int(11) DEFAULT NULL,
  `passText` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`idUsuario`),
  UNIQUE KEY `usuario` (`usuario`),
  KEY `FK_usuarios_idComercial` (`idComercial`),
  KEY `FK_usuarios_idDelegacion` (`idDelegacion`),
  KEY `FK_usuarios_idNutricionista` (`idNutricionista`),
  KEY `FK_usuarios_idDireccion` (`idDireccion`),
  CONSTRAINT `FK_usuarios_idComercial` FOREIGN KEY (`idComercial`) REFERENCES `comerciales` (`idComercial`),
  CONSTRAINT `FK_usuarios_idDelegacion` FOREIGN KEY (`idDelegacion`) REFERENCES `delegaciones` (`idDelegacion`),
  CONSTRAINT `FK_usuarios_idDireccion` FOREIGN KEY (`idDireccion`) REFERENCES `direcciones` (`idDireccion`),
  CONSTRAINT `FK_usuarios_idNutricionista` FOREIGN KEY (`idNutricionista`) REFERENCES `nutricionistas` (`idNutricionista`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vencimientos`
--

DROP TABLE IF EXISTS `vencimientos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vencimientos` (
  `idVencimiento` int(11) NOT NULL,
  `generado` tinyint(1) DEFAULT '0',
  `importe` double DEFAULT NULL,
  `pagoCobro` int(11) DEFAULT NULL,
  `contabilidad` tinyint(1) DEFAULT '0',
  `tipo` int(11) DEFAULT NULL,
  `pagado` tinyint(1) DEFAULT '0',
  `fechaVencimiento` date NOT NULL,
  `estado` int(11) DEFAULT NULL,
  `idFormaPago` int(11) DEFAULT NULL,
  `idFactura` int(11) DEFAULT NULL,
  PRIMARY KEY (`idVencimiento`),
  KEY `FK_vencimientos_idFactura` (`idFactura`),
  KEY `FK_vencimientos_idFormaPago` (`idFormaPago`),
  CONSTRAINT `FK_vencimientos_idFactura` FOREIGN KEY (`idFactura`) REFERENCES `factura` (`idFactura`),
  CONSTRAINT `FK_vencimientos_idFormaPago` FOREIGN KEY (`idFormaPago`) REFERENCES `formapago` (`idFormaPago`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `visitas`
--

DROP TABLE IF EXISTS `visitas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `visitas` (
  `idVisita` int(11) NOT NULL,
  `fechaVisita` date DEFAULT NULL,
  `idNutricionista` int(11) DEFAULT NULL,
  `idCliente` int(11) NOT NULL,
  `nuevas` int(10) unsigned NOT NULL,
  `promocionales` int(10) unsigned NOT NULL,
  `revisiones` int(10) unsigned NOT NULL,
  `captaciones` int(10) unsigned NOT NULL,
  `codigoQpnut` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`idVisita`),
  KEY `FK_visitas_idCliente` (`idCliente`),
  KEY `FK_visitas_idNutricionista` (`idNutricionista`),
  CONSTRAINT `FK_visitas_idCliente` FOREIGN KEY (`idCliente`) REFERENCES `clientes` (`idCliente`),
  CONSTRAINT `FK_visitas_idNutricionista` FOREIGN KEY (`idNutricionista`) REFERENCES `nutricionistas` (`idNutricionista`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `visitasproductos`
--

DROP TABLE IF EXISTS `visitasproductos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `visitasproductos` (
  `idVisitaProductos` int(11) NOT NULL,
  `salida` int(11) NOT NULL,
  `entrada` int(11) NOT NULL,
  `idVisita` int(11) NOT NULL,
  `idProducto` int(11) NOT NULL,
  `idSesion` int(11) DEFAULT NULL,
  PRIMARY KEY (`idVisitaProductos`),
  KEY `FK_visitasproductos_idProducto` (`idProducto`),
  KEY `FK_visitasproductos_idSesion` (`idSesion`),
  KEY `FK_visitasproductos_idVisita` (`idVisita`),
  CONSTRAINT `FK_visitasproductos_idProducto` FOREIGN KEY (`idProducto`) REFERENCES `productos` (`idProducto`),
  CONSTRAINT `FK_visitasproductos_idSesion` FOREIGN KEY (`idSesion`) REFERENCES `sesiones` (`idSesion`),
  CONSTRAINT `FK_visitasproductos_idVisita` FOREIGN KEY (`idVisita`) REFERENCES `visitas` (`idVisita`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `zonas`
--

DROP TABLE IF EXISTS `zonas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `zonas` (
  `idZona` int(11) NOT NULL,
  `nombre` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`idZona`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-11 16:22:52
