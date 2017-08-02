/*
 * Unless explicitly acquired and licensed from Licensor under another license, the contents of
 * this file are subject to the Reciprocal Public License ("RPL") Version 1.5, or subsequent
 * versions as allowed by the RPL, and You may not copy or use this file in either source code
 * or executable form, except in compliance with the terms and conditions of the RPL
 *
 * All software distributed under the RPL is provided strictly on an "AS IS" basis, WITHOUT
 * WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, AND LICENSOR HEREBY DISCLAIMS ALL SUCH
 * WARRANTIES, INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, QUIET ENJOYMENT, OR NON-INFRINGEMENT. See the RPL for specific language
 * governing rights and limitations under the RPL.
 *
 * http://opensource.org/licenses/RPL-1.5
 *
 * Copyright 2012-2017 Open Justice Broker Consortium
 */
create table F1(
	F1_id INT NOT NULL,
	D1_id INT NOT NULL
);
create table D1(
	D1_id INT NOT NULL,
	D1_description VARCHAR(10) NOT NULL
);
create table F2(
	F2_id INT NOT NULL,
	D2_id INT NOT NULL
);
create table D2(
	D2_id INT NOT NULL,
	D2_description VARCHAR(10) NOT NULL
);
create table F3(
	F3_id INT NOT NULL,
	D1_id INT NOT NULL,
	D2_id INT NOT NULL,
	F3_value NUMERIC
);
insert into F1 values
	(1,1),
	(2,1),
	(3,2)
;
insert into D1 values
	(1,'D1 One'),
	(2,'D1 Two')
;
insert into F2 values
	(1,1),
	(2,2),
	(3,3)
;
insert into D2 values
	(1,'D2 One'),
	(2,'D2 Two'),
	(3,'D2 Three')
;
insert into F3 values
	(1,1,1,10),
	(2,1,2,20),
	(3,2,3,35)
;
