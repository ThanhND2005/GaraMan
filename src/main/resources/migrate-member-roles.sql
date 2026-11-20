-- Run with psql against a database using the four employee subtype tables.
\set ON_ERROR_STOP on
BEGIN;
LOCK TABLE tblMember2, tblManager2, tblSalesStaff2, tblWarehouseStaff2, tblTechnician2,
    tblServiceRequest2, tblServiceRequestDetail2, tblInvoice2, tblImportOrder2 IN ACCESS EXCLUSIVE MODE;
ALTER TABLE tblMember2 ADD COLUMN role varchar(20), ADD COLUMN position varchar(50),
    ADD COLUMN specialty varchar(100), ADD COLUMN grade varchar(50);
CREATE TEMP TABLE employee_profile (
    id bigint PRIMARY KEY, role varchar(20), position varchar(50), specialty varchar(100), grade varchar(50)
) ON COMMIT DROP;
INSERT INTO employee_profile SELECT id,'MANAGER',position,NULL,NULL FROM tblManager2;
INSERT INTO employee_profile SELECT id,'SALES_STAFF',position,NULL,NULL FROM tblSalesStaff2;
INSERT INTO employee_profile SELECT id,'WAREHOUSE_STAFF',position,NULL,NULL FROM tblWarehouseStaff2;
INSERT INTO employee_profile SELECT id,'TECHNICIAN',NULL,specialty,grade FROM tblTechnician2;

UPDATE tblMember2 m SET role=p.role, position=p.position, specialty=p.specialty, grade=p.grade
FROM employee_profile p WHERE m.id=p.id;
ALTER TABLE tblMember2 ALTER COLUMN role SET NOT NULL, ADD CONSTRAINT member_profile CHECK (
        (role IN ('MANAGER','SALES_STAFF','WAREHOUSE_STAFF') AND position IS NOT NULL AND specialty IS NULL AND grade IS NULL)
        OR (role='TECHNICIAN' AND position IS NULL AND specialty IS NOT NULL AND grade IS NOT NULL)
    );
ALTER TABLE tblServiceRequest2 DROP CONSTRAINT tblServiceRequest2_receivedById_fkey,
    ADD FOREIGN KEY (receivedById) REFERENCES tblMember2(id);
ALTER TABLE tblServiceRequestDetail2 DROP CONSTRAINT tblServiceRequestDetail2_technicianId_fkey,
    ADD FOREIGN KEY (technicianId) REFERENCES tblMember2(id);
ALTER TABLE tblInvoice2 DROP CONSTRAINT tblInvoice2_createdById_fkey,
    ADD FOREIGN KEY (createdById) REFERENCES tblMember2(id);
ALTER TABLE tblInvoice2 DROP CONSTRAINT tblInvoice2_receivedById_fkey,
    ADD FOREIGN KEY (receivedById) REFERENCES tblMember2(id);
ALTER TABLE tblImportOrder2 DROP CONSTRAINT tblImportOrder2_createdById_fkey,
    ADD FOREIGN KEY (createdById) REFERENCES tblMember2(id);
-- Retain original profiles for verification after cutover.
CREATE SCHEMA garaman_legacy;
ALTER TABLE tblManager2 SET SCHEMA garaman_legacy;
ALTER TABLE tblSalesStaff2 SET SCHEMA garaman_legacy;
ALTER TABLE tblWarehouseStaff2 SET SCHEMA garaman_legacy;
ALTER TABLE tblTechnician2 SET SCHEMA garaman_legacy;
\ir schema.sql
COMMIT;
