-- Epicentro Gourmet Hito 2: INSERTs para una base de demostración MySQL 8.
-- Primero crear el esquema con la aplicación (ddl-auto=update). Seleccionar una base DEMO.
-- No incluye DROP, DELETE, TRUNCATE ni reemplazo de TestCargarBD.
-- Es repetible: reutiliza las claves de demostración y no cambia cuentas preexistentes.
-- Cuentas de demostración: contraseña DemoHito2! (hash BCrypt, exclusivamente para defensa).
-- Si se carga otro día, usar una nueva base demo para disponer de un festival vigente.
SET NAMES utf8mb4;
START TRANSACTION;


INSERT INTO rol(nombre) SELECT 'ADMIN' WHERE NOT EXISTS(SELECT 1 FROM rol WHERE nombre='ADMIN');

INSERT INTO rol(nombre) SELECT 'RESPONSABLE' WHERE NOT EXISTS(SELECT 1 FROM rol WHERE nombre='RESPONSABLE');

INSERT INTO rol(nombre) SELECT 'EMPLEADO' WHERE NOT EXISTS(SELECT 1 FROM rol WHERE nombre='EMPLEADO');

INSERT INTO costo(costo_por_superficie,costo_por_montaje,plus_electricidad,sueldo_base,monto_anio_antiguedad,plus_cocinero,plus_cocinero_ayudante,plus_lavaplatos)
SELECT 500,10,2000,100000,5000,150000,100000,50000 WHERE NOT EXISTS(SELECT 1 FROM festival WHERE nombre='Festival Demo Epicentro');
SET @costo_vig=COALESCE((SELECT costo_id FROM festival WHERE nombre='Festival Demo Epicentro' ORDER BY id LIMIT 1),LAST_INSERT_ID());
INSERT INTO festival(nombre,temporada,fecha_inicio,fecha_fin,costo_id)
SELECT 'Festival Demo Epicentro','Demostración',DATE_SUB(CURRENT_DATE, INTERVAL 10 DAY),DATE_ADD(CURRENT_DATE, INTERVAL 10 DAY),@costo_vig WHERE NOT EXISTS(SELECT 1 FROM festival WHERE nombre='Festival Demo Epicentro');
SET @f_vig=(SELECT id FROM festival WHERE nombre='Festival Demo Epicentro' ORDER BY id LIMIT 1);

INSERT INTO costo(costo_por_superficie,costo_por_montaje,plus_electricidad,sueldo_base,monto_anio_antiguedad,plus_cocinero,plus_cocinero_ayudante,plus_lavaplatos)
SELECT 500,10,2000,100000,5000,150000,100000,50000 WHERE NOT EXISTS(SELECT 1 FROM festival WHERE nombre='Festival Demo Anterior');
SET @costo_ant=COALESCE((SELECT costo_id FROM festival WHERE nombre='Festival Demo Anterior' ORDER BY id LIMIT 1),LAST_INSERT_ID());
INSERT INTO festival(nombre,temporada,fecha_inicio,fecha_fin,costo_id)
SELECT 'Festival Demo Anterior','Demostración',DATE_SUB(CURRENT_DATE, INTERVAL 60 DAY),DATE_SUB(CURRENT_DATE, INTERVAL 40 DAY),@costo_ant WHERE NOT EXISTS(SELECT 1 FROM festival WHERE nombre='Festival Demo Anterior');
SET @f_ant=(SELECT id FROM festival WHERE nombre='Festival Demo Anterior' ORDER BY id LIMIT 1);

INSERT INTO unidad_venta(nombre_comercial,superficie,codigo,responsable_id)
SELECT 'Food Truck Demo Epicentro',20,'DE00000001',NULL WHERE NOT EXISTS(SELECT 1 FROM unidad_venta WHERE codigo='DE00000001');
SET @u_a=(SELECT id FROM unidad_venta WHERE codigo='DE00000001');

INSERT INTO food_truck(id,patente,requiere_electricidad) SELECT @u_a,'DEMO-001',TRUE WHERE NOT EXISTS(SELECT 1 FROM food_truck WHERE id=@u_a);

INSERT INTO festival_unidades_de_venta(festivales_id,unidades_de_venta_id)
SELECT @f_vig,@u_a WHERE NOT EXISTS(SELECT 1 FROM festival_unidades_de_venta WHERE festivales_id=@f_vig AND unidades_de_venta_id=@u_a);

INSERT INTO festival_unidades_de_venta(festivales_id,unidades_de_venta_id)
SELECT @f_ant,@u_a WHERE NOT EXISTS(SELECT 1 FROM festival_unidades_de_venta WHERE festivales_id=@f_ant AND unidades_de_venta_id=@u_a);

INSERT INTO unidad_venta(nombre_comercial,superficie,codigo,responsable_id)
SELECT 'Puesto Demo Epicentro',30,'DE00000002',NULL WHERE NOT EXISTS(SELECT 1 FROM unidad_venta WHERE codigo='DE00000002');
SET @u_b=(SELECT id FROM unidad_venta WHERE codigo='DE00000002');

INSERT INTO puesto_desarmable(id,cantidad_carpas,tiempo_montaje) SELECT @u_b,3,60 WHERE NOT EXISTS(SELECT 1 FROM puesto_desarmable WHERE id=@u_b);

INSERT INTO festival_unidades_de_venta(festivales_id,unidades_de_venta_id)
SELECT @f_vig,@u_b WHERE NOT EXISTS(SELECT 1 FROM festival_unidades_de_venta WHERE festivales_id=@f_vig AND unidades_de_venta_id=@u_b);

INSERT INTO festival_unidades_de_venta(festivales_id,unidades_de_venta_id)
SELECT @f_ant,@u_b WHERE NOT EXISTS(SELECT 1 FROM festival_unidades_de_venta WHERE festivales_id=@f_ant AND unidades_de_venta_id=@u_b);

INSERT INTO usuario(username,password,rol_id)
SELECT 'admin.demo','$2a$10$ozzE/aMk76P4r9ZUl1yJiuEFd/bss2mv3ZHQ0j5C3sv0Ccr7Y5vmq',(SELECT id FROM rol WHERE nombre='ADMIN' ORDER BY id LIMIT 1)
WHERE NOT EXISTS(SELECT 1 FROM usuario WHERE username='admin.demo');
SET @cuenta_admin=(SELECT id FROM usuario WHERE username='admin.demo');

INSERT INTO usuario(username,password,rol_id)
SELECT 'responsable.demo','$2a$10$ozzE/aMk76P4r9ZUl1yJiuEFd/bss2mv3ZHQ0j5C3sv0Ccr7Y5vmq',(SELECT id FROM rol WHERE nombre='RESPONSABLE' ORDER BY id LIMIT 1)
WHERE NOT EXISTS(SELECT 1 FROM usuario WHERE username='responsable.demo');
SET @cuenta_resp_a=(SELECT id FROM usuario WHERE username='responsable.demo');

INSERT INTO empleado(nombre,apellido,dni,fecha_nacimiento,fecha_ingreso,sueldo_base,activo,unidad_venta_id,usuario_id)
SELECT 'Ana','Peña',70100001,'1990-01-10','2020-01-01',100000,TRUE,@u_a,@cuenta_resp_a
WHERE NOT EXISTS(SELECT 1 FROM empleado WHERE usuario_id=@cuenta_resp_a OR dni=70100001);
SET @e_resp_a=(SELECT id FROM empleado WHERE usuario_id=@cuenta_resp_a ORDER BY id LIMIT 1);

INSERT INTO usuario(username,password,rol_id)
SELECT 'responsable2.demo','$2a$10$ozzE/aMk76P4r9ZUl1yJiuEFd/bss2mv3ZHQ0j5C3sv0Ccr7Y5vmq',(SELECT id FROM rol WHERE nombre='RESPONSABLE' ORDER BY id LIMIT 1)
WHERE NOT EXISTS(SELECT 1 FROM usuario WHERE username='responsable2.demo');
SET @cuenta_resp_b=(SELECT id FROM usuario WHERE username='responsable2.demo');

INSERT INTO empleado(nombre,apellido,dni,fecha_nacimiento,fecha_ingreso,sueldo_base,activo,unidad_venta_id,usuario_id)
SELECT 'Diego','Muñoz',70100002,'1990-01-10','2020-01-01',100000,TRUE,@u_b,@cuenta_resp_b
WHERE NOT EXISTS(SELECT 1 FROM empleado WHERE usuario_id=@cuenta_resp_b OR dni=70100002);
SET @e_resp_b=(SELECT id FROM empleado WHERE usuario_id=@cuenta_resp_b ORDER BY id LIMIT 1);

INSERT INTO usuario(username,password,rol_id)
SELECT 'cajero.demo','$2a$10$ozzE/aMk76P4r9ZUl1yJiuEFd/bss2mv3ZHQ0j5C3sv0Ccr7Y5vmq',(SELECT id FROM rol WHERE nombre='EMPLEADO' ORDER BY id LIMIT 1)
WHERE NOT EXISTS(SELECT 1 FROM usuario WHERE username='cajero.demo');
SET @cuenta_caja_a=(SELECT id FROM usuario WHERE username='cajero.demo');

INSERT INTO empleado(nombre,apellido,dni,fecha_nacimiento,fecha_ingreso,sueldo_base,activo,unidad_venta_id,usuario_id)
SELECT 'Lucía','Paz',70100003,'1990-01-10','2020-01-01',100000,TRUE,@u_a,@cuenta_caja_a
WHERE NOT EXISTS(SELECT 1 FROM empleado WHERE usuario_id=@cuenta_caja_a OR dni=70100003);
SET @e_caja_a=(SELECT id FROM empleado WHERE usuario_id=@cuenta_caja_a ORDER BY id LIMIT 1);

INSERT INTO cajero(id,turno) SELECT @e_caja_a,'Mañana' WHERE NOT EXISTS(SELECT 1 FROM cajero WHERE id=@e_caja_a);

INSERT INTO usuario(username,password,rol_id)
SELECT 'cocinero.demo','$2a$10$ozzE/aMk76P4r9ZUl1yJiuEFd/bss2mv3ZHQ0j5C3sv0Ccr7Y5vmq',(SELECT id FROM rol WHERE nombre='EMPLEADO' ORDER BY id LIMIT 1)
WHERE NOT EXISTS(SELECT 1 FROM usuario WHERE username='cocinero.demo');
SET @cuenta_cocina_a=(SELECT id FROM usuario WHERE username='cocinero.demo');

INSERT INTO empleado(nombre,apellido,dni,fecha_nacimiento,fecha_ingreso,sueldo_base,activo,unidad_venta_id,usuario_id)
SELECT 'Matías','Silva',70100004,'1990-01-10','2020-01-01',100000,TRUE,@u_a,@cuenta_cocina_a
WHERE NOT EXISTS(SELECT 1 FROM empleado WHERE usuario_id=@cuenta_cocina_a OR dni=70100004);
SET @e_cocina_a=(SELECT id FROM empleado WHERE usuario_id=@cuenta_cocina_a ORDER BY id LIMIT 1);

INSERT INTO cocinero(id,categoria,especialidad_culinaria,plus_categoria) SELECT @e_cocina_a,0,'Cocina de demostración',150000 WHERE NOT EXISTS(SELECT 1 FROM cocinero WHERE id=@e_cocina_a);

INSERT INTO usuario(username,password,rol_id)
SELECT 'ayudante.demo','$2a$10$ozzE/aMk76P4r9ZUl1yJiuEFd/bss2mv3ZHQ0j5C3sv0Ccr7Y5vmq',(SELECT id FROM rol WHERE nombre='EMPLEADO' ORDER BY id LIMIT 1)
WHERE NOT EXISTS(SELECT 1 FROM usuario WHERE username='ayudante.demo');
SET @cuenta_ayuda_a=(SELECT id FROM usuario WHERE username='ayudante.demo');

INSERT INTO empleado(nombre,apellido,dni,fecha_nacimiento,fecha_ingreso,sueldo_base,activo,unidad_venta_id,usuario_id)
SELECT 'Micaela','Benítez',70100005,'1990-01-10','2020-01-01',100000,TRUE,@u_a,@cuenta_ayuda_a
WHERE NOT EXISTS(SELECT 1 FROM empleado WHERE usuario_id=@cuenta_ayuda_a OR dni=70100005);
SET @e_ayuda_a=(SELECT id FROM empleado WHERE usuario_id=@cuenta_ayuda_a ORDER BY id LIMIT 1);

INSERT INTO cocinero(id,categoria,especialidad_culinaria,plus_categoria) SELECT @e_ayuda_a,1,'Cocina de demostración',100000 WHERE NOT EXISTS(SELECT 1 FROM cocinero WHERE id=@e_ayuda_a);

INSERT INTO usuario(username,password,rol_id)
SELECT 'lavaplatos.demo','$2a$10$ozzE/aMk76P4r9ZUl1yJiuEFd/bss2mv3ZHQ0j5C3sv0Ccr7Y5vmq',(SELECT id FROM rol WHERE nombre='EMPLEADO' ORDER BY id LIMIT 1)
WHERE NOT EXISTS(SELECT 1 FROM usuario WHERE username='lavaplatos.demo');
SET @cuenta_lava_a=(SELECT id FROM usuario WHERE username='lavaplatos.demo');

INSERT INTO empleado(nombre,apellido,dni,fecha_nacimiento,fecha_ingreso,sueldo_base,activo,unidad_venta_id,usuario_id)
SELECT 'Ramiro','Vega',70100006,'1990-01-10','2020-01-01',100000,TRUE,@u_a,@cuenta_lava_a
WHERE NOT EXISTS(SELECT 1 FROM empleado WHERE usuario_id=@cuenta_lava_a OR dni=70100006);
SET @e_lava_a=(SELECT id FROM empleado WHERE usuario_id=@cuenta_lava_a ORDER BY id LIMIT 1);

INSERT INTO cocinero(id,categoria,especialidad_culinaria,plus_categoria) SELECT @e_lava_a,2,'Cocina de demostración',50000 WHERE NOT EXISTS(SELECT 1 FROM cocinero WHERE id=@e_lava_a);

INSERT INTO usuario(username,password,rol_id)
SELECT 'cajero2.demo','$2a$10$ozzE/aMk76P4r9ZUl1yJiuEFd/bss2mv3ZHQ0j5C3sv0Ccr7Y5vmq',(SELECT id FROM rol WHERE nombre='EMPLEADO' ORDER BY id LIMIT 1)
WHERE NOT EXISTS(SELECT 1 FROM usuario WHERE username='cajero2.demo');
SET @cuenta_caja_b=(SELECT id FROM usuario WHERE username='cajero2.demo');

INSERT INTO empleado(nombre,apellido,dni,fecha_nacimiento,fecha_ingreso,sueldo_base,activo,unidad_venta_id,usuario_id)
SELECT 'Lucas','Moreno',70100007,'1990-01-10','2020-01-01',100000,TRUE,@u_b,@cuenta_caja_b
WHERE NOT EXISTS(SELECT 1 FROM empleado WHERE usuario_id=@cuenta_caja_b OR dni=70100007);
SET @e_caja_b=(SELECT id FROM empleado WHERE usuario_id=@cuenta_caja_b ORDER BY id LIMIT 1);

INSERT INTO cajero(id,turno) SELECT @e_caja_b,'Mañana' WHERE NOT EXISTS(SELECT 1 FROM cajero WHERE id=@e_caja_b);

INSERT INTO usuario(username,password,rol_id)
SELECT 'ayudante2.demo','$2a$10$ozzE/aMk76P4r9ZUl1yJiuEFd/bss2mv3ZHQ0j5C3sv0Ccr7Y5vmq',(SELECT id FROM rol WHERE nombre='EMPLEADO' ORDER BY id LIMIT 1)
WHERE NOT EXISTS(SELECT 1 FROM usuario WHERE username='ayudante2.demo');
SET @cuenta_ayuda_b=(SELECT id FROM usuario WHERE username='ayudante2.demo');

INSERT INTO empleado(nombre,apellido,dni,fecha_nacimiento,fecha_ingreso,sueldo_base,activo,unidad_venta_id,usuario_id)
SELECT 'Paula','Acosta',70100008,'1990-01-10','2020-01-01',100000,TRUE,@u_b,@cuenta_ayuda_b
WHERE NOT EXISTS(SELECT 1 FROM empleado WHERE usuario_id=@cuenta_ayuda_b OR dni=70100008);
SET @e_ayuda_b=(SELECT id FROM empleado WHERE usuario_id=@cuenta_ayuda_b ORDER BY id LIMIT 1);

INSERT INTO cocinero(id,categoria,especialidad_culinaria,plus_categoria) SELECT @e_ayuda_b,1,'Cocina de demostración',100000 WHERE NOT EXISTS(SELECT 1 FROM cocinero WHERE id=@e_ayuda_b);

INSERT INTO usuario(username,password,rol_id)
SELECT 'inactivo.demo','$2a$10$ozzE/aMk76P4r9ZUl1yJiuEFd/bss2mv3ZHQ0j5C3sv0Ccr7Y5vmq',(SELECT id FROM rol WHERE nombre='EMPLEADO' ORDER BY id LIMIT 1)
WHERE NOT EXISTS(SELECT 1 FROM usuario WHERE username='inactivo.demo');
SET @cuenta_inactivo=(SELECT id FROM usuario WHERE username='inactivo.demo');

INSERT INTO empleado(nombre,apellido,dni,fecha_nacimiento,fecha_ingreso,sueldo_base,activo,unidad_venta_id,usuario_id)
SELECT 'Hernán','Quiroga',70100009,'1990-01-10','2020-01-01',100000,FALSE,@u_a,@cuenta_inactivo
WHERE NOT EXISTS(SELECT 1 FROM empleado WHERE usuario_id=@cuenta_inactivo OR dni=70100009);
SET @e_inactivo=(SELECT id FROM empleado WHERE usuario_id=@cuenta_inactivo ORDER BY id LIMIT 1);

INSERT INTO cajero(id,turno) SELECT @e_inactivo,'Mañana' WHERE NOT EXISTS(SELECT 1 FROM cajero WHERE id=@e_inactivo);

UPDATE unidad_venta SET responsable_id=@e_resp_a WHERE id=@u_a AND responsable_id IS NULL;

UPDATE unidad_venta SET responsable_id=@e_resp_b WHERE id=@u_b AND responsable_id IS NULL;

INSERT INTO plato(nombre,precio_venta,costo_produccion) SELECT 'Hamburguesa Demo Epicentro',8500,4000
WHERE NOT EXISTS(SELECT 1 FROM plato WHERE nombre='Hamburguesa Demo Epicentro');
SET @p_hamb=(SELECT id FROM plato WHERE nombre='Hamburguesa Demo Epicentro' ORDER BY id LIMIT 1);

INSERT INTO unidad_venta_platos(unidad_venta_id,platos_id) SELECT @u_a,@p_hamb
WHERE NOT EXISTS(SELECT 1 FROM unidad_venta_platos WHERE unidad_venta_id=@u_a AND platos_id=@p_hamb);

INSERT INTO plato(nombre,precio_venta,costo_produccion) SELECT 'Empanada Demo Epicentro',3500,1200
WHERE NOT EXISTS(SELECT 1 FROM plato WHERE nombre='Empanada Demo Epicentro');
SET @p_emp=(SELECT id FROM plato WHERE nombre='Empanada Demo Epicentro' ORDER BY id LIMIT 1);

INSERT INTO unidad_venta_platos(unidad_venta_id,platos_id) SELECT @u_a,@p_emp
WHERE NOT EXISTS(SELECT 1 FROM unidad_venta_platos WHERE unidad_venta_id=@u_a AND platos_id=@p_emp);

INSERT INTO unidad_venta_platos(unidad_venta_id,platos_id) SELECT @u_b,@p_emp
WHERE NOT EXISTS(SELECT 1 FROM unidad_venta_platos WHERE unidad_venta_id=@u_b AND platos_id=@p_emp);

INSERT INTO plato(nombre,precio_venta,costo_produccion) SELECT 'Plato Demo Criollo',9500,4500
WHERE NOT EXISTS(SELECT 1 FROM plato WHERE nombre='Plato Demo Criollo');
SET @p_plato=(SELECT id FROM plato WHERE nombre='Plato Demo Criollo' ORDER BY id LIMIT 1);

INSERT INTO unidad_venta_platos(unidad_venta_id,platos_id) SELECT @u_b,@p_plato
WHERE NOT EXISTS(SELECT 1 FROM unidad_venta_platos WHERE unidad_venta_id=@u_b AND platos_id=@p_plato);

-- Canon: superficie x costo + electricidad (FoodTruck) o - minutos x montaje (Puesto).
INSERT INTO canon_unidad_festival(festival_id,unidad_venta_id,importe)
SELECT f.id,u.id,ROUND(u.superficie*c.costo_por_superficie+
CASE WHEN ft.id IS NOT NULL THEN IF(ft.requiere_electricidad,c.plus_electricidad,0) ELSE -pd.tiempo_montaje*c.costo_por_montaje END,2)
FROM festival f JOIN costo c ON c.id=f.costo_id JOIN festival_unidades_de_venta fu ON fu.festivales_id=f.id
JOIN unidad_venta u ON u.id=fu.unidades_de_venta_id LEFT JOIN food_truck ft ON ft.id=u.id LEFT JOIN puesto_desarmable pd ON pd.id=u.id
WHERE f.id IN (@f_vig,@f_ant) AND u.id IN (@u_a,@u_b)
AND NOT EXISTS(SELECT 1 FROM canon_unidad_festival h WHERE h.festival_id=f.id AND h.unidad_venta_id=u.id);

-- Liquidaciones: mismos tramos inclusivos, adicionales y fórmula >30 días que el proyecto.
INSERT INTO asignacion_laboral(empleado_id,festival_id,unidad_venta_id,fecha_inicio,fecha_fin,sueldo_liquidado)
SELECT e.id,f.id,e.unidad_venta_id,f.fecha_inicio,f.fecha_fin,
ROUND((c.sueldo_base+CASE WHEN cj.id IS NOT NULL THEN TIMESTAMPDIFF(YEAR,e.fecha_ingreso,f.fecha_inicio)*c.monto_anio_antiguedad
WHEN cc.id IS NOT NULL THEN CASE cc.categoria WHEN 0 THEN c.plus_cocinero WHEN 1 THEN c.plus_cocinero_ayudante ELSE c.plus_lavaplatos END ELSE 0 END)
*CASE WHEN DATEDIFF(f.fecha_fin,f.fecha_inicio)+1<=15 THEN 0.5 WHEN DATEDIFF(f.fecha_fin,f.fecha_inicio)+1<=30 THEN 1 ELSE (FLOOR((DATEDIFF(f.fecha_fin,f.fecha_inicio)+1)/15)+1)*0.5 END,2)
FROM empleado e JOIN festival_unidades_de_venta fu ON fu.unidades_de_venta_id=e.unidad_venta_id JOIN festival f ON f.id=fu.festivales_id
JOIN costo c ON c.id=f.costo_id LEFT JOIN cajero cj ON cj.id=e.id LEFT JOIN cocinero cc ON cc.id=e.id
WHERE f.id IN (@f_vig,@f_ant) AND e.id IN (@e_resp_a,@e_resp_b,@e_caja_a,@e_cocina_a,@e_ayuda_a,@e_lava_a,@e_caja_b,@e_ayuda_b,@e_inactivo)
AND (e.activo=TRUE OR f.id=@f_ant)
AND NOT EXISTS(SELECT 1 FROM asignacion_laboral a WHERE a.empleado_id=e.id AND a.festival_id=f.id AND a.unidad_venta_id=e.unidad_venta_id AND a.fecha_inicio=f.fecha_inicio);
SET @dia_demo=DATE_ADD((SELECT fecha_inicio FROM festival WHERE id=@f_vig), INTERVAL 10 DAY);
SET @dia_ant=(SELECT fecha_fin FROM festival WHERE id=@f_ant);


INSERT INTO pedido(fecha_transaccion,festival_id,unidad_venta_id) SELECT TIMESTAMP(@dia_demo,'12:01:00'),@f_vig,@u_a
WHERE NOT EXISTS(SELECT 1 FROM pedido WHERE festival_id=@f_vig AND unidad_venta_id=@u_a AND fecha_transaccion=TIMESTAMP(@dia_demo,'12:01:00'));
SET @pedido_1=(SELECT id FROM pedido WHERE festival_id=@f_vig AND unidad_venta_id=@u_a AND fecha_transaccion=TIMESTAMP(@dia_demo,'12:01:00') ORDER BY id LIMIT 1);

INSERT INTO detalle_pedido(cantidad,precio_unitario,costo_unitario,plato_id,pedido_id)
SELECT 2,precio_venta,costo_produccion,id,@pedido_1 FROM plato WHERE id=@p_hamb
AND NOT EXISTS(SELECT 1 FROM detalle_pedido WHERE pedido_id=@pedido_1 AND plato_id=@p_hamb);

INSERT INTO detalle_pedido(cantidad,precio_unitario,costo_unitario,plato_id,pedido_id)
SELECT 3,precio_venta,costo_produccion,id,@pedido_1 FROM plato WHERE id=@p_emp
AND NOT EXISTS(SELECT 1 FROM detalle_pedido WHERE pedido_id=@pedido_1 AND plato_id=@p_emp);

INSERT INTO pedido(fecha_transaccion,festival_id,unidad_venta_id) SELECT TIMESTAMP(@dia_demo,'12:02:00'),@f_vig,@u_b
WHERE NOT EXISTS(SELECT 1 FROM pedido WHERE festival_id=@f_vig AND unidad_venta_id=@u_b AND fecha_transaccion=TIMESTAMP(@dia_demo,'12:02:00'));
SET @pedido_2=(SELECT id FROM pedido WHERE festival_id=@f_vig AND unidad_venta_id=@u_b AND fecha_transaccion=TIMESTAMP(@dia_demo,'12:02:00') ORDER BY id LIMIT 1);

INSERT INTO detalle_pedido(cantidad,precio_unitario,costo_unitario,plato_id,pedido_id)
SELECT 1,precio_venta,costo_produccion,id,@pedido_2 FROM plato WHERE id=@p_plato
AND NOT EXISTS(SELECT 1 FROM detalle_pedido WHERE pedido_id=@pedido_2 AND plato_id=@p_plato);

INSERT INTO detalle_pedido(cantidad,precio_unitario,costo_unitario,plato_id,pedido_id)
SELECT 4,precio_venta,costo_produccion,id,@pedido_2 FROM plato WHERE id=@p_emp
AND NOT EXISTS(SELECT 1 FROM detalle_pedido WHERE pedido_id=@pedido_2 AND plato_id=@p_emp);

INSERT INTO pedido(fecha_transaccion,festival_id,unidad_venta_id) SELECT TIMESTAMP(@dia_ant,'12:03:00'),@f_ant,@u_a
WHERE NOT EXISTS(SELECT 1 FROM pedido WHERE festival_id=@f_ant AND unidad_venta_id=@u_a AND fecha_transaccion=TIMESTAMP(@dia_ant,'12:03:00'));
SET @pedido_3=(SELECT id FROM pedido WHERE festival_id=@f_ant AND unidad_venta_id=@u_a AND fecha_transaccion=TIMESTAMP(@dia_ant,'12:03:00') ORDER BY id LIMIT 1);

INSERT INTO detalle_pedido(cantidad,precio_unitario,costo_unitario,plato_id,pedido_id)
SELECT 1,precio_venta,costo_produccion,id,@pedido_3 FROM plato WHERE id=@p_hamb
AND NOT EXISTS(SELECT 1 FROM detalle_pedido WHERE pedido_id=@pedido_3 AND plato_id=@p_hamb);

INSERT INTO detalle_pedido(cantidad,precio_unitario,costo_unitario,plato_id,pedido_id)
SELECT 2,precio_venta,costo_produccion,id,@pedido_3 FROM plato WHERE id=@p_emp
AND NOT EXISTS(SELECT 1 FROM detalle_pedido WHERE pedido_id=@pedido_3 AND plato_id=@p_emp);

INSERT INTO pedido(fecha_transaccion,festival_id,unidad_venta_id) SELECT TIMESTAMP(@dia_ant,'12:04:00'),@f_ant,@u_b
WHERE NOT EXISTS(SELECT 1 FROM pedido WHERE festival_id=@f_ant AND unidad_venta_id=@u_b AND fecha_transaccion=TIMESTAMP(@dia_ant,'12:04:00'));
SET @pedido_4=(SELECT id FROM pedido WHERE festival_id=@f_ant AND unidad_venta_id=@u_b AND fecha_transaccion=TIMESTAMP(@dia_ant,'12:04:00') ORDER BY id LIMIT 1);

INSERT INTO detalle_pedido(cantidad,precio_unitario,costo_unitario,plato_id,pedido_id)
SELECT 2,precio_venta,costo_produccion,id,@pedido_4 FROM plato WHERE id=@p_plato
AND NOT EXISTS(SELECT 1 FROM detalle_pedido WHERE pedido_id=@pedido_4 AND plato_id=@p_plato);

INSERT INTO detalle_pedido(cantidad,precio_unitario,costo_unitario,plato_id,pedido_id)
SELECT 1,precio_venta,costo_produccion,id,@pedido_4 FROM plato WHERE id=@p_emp
AND NOT EXISTS(SELECT 1 FROM detalle_pedido WHERE pedido_id=@pedido_4 AND plato_id=@p_emp);

INSERT INTO cierre_caja(unidad_venta_id,cajero_id,fecha,cerrado_en,total)
SELECT @u_a,@e_caja_a,@dia_ant,TIMESTAMP(@dia_ant,'23:00:00'),SUM(d.cantidad*d.precio_unitario)
FROM detalle_pedido d JOIN pedido p ON p.id=d.pedido_id WHERE p.id=@pedido_3
HAVING NOT EXISTS(SELECT 1 FROM cierre_caja WHERE unidad_venta_id=@u_a AND fecha=@dia_ant);
COMMIT;
SELECT 'Datos de demostración cargados; contraseña de las cuentas nuevas: DemoHito2!' AS resultado;
