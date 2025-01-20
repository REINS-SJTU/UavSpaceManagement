--
-- PostgreSQL database dump
--

-- Dumped from database version 17.0
-- Dumped by pg_dump version 17.0

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: h_position; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.h_position (
    human_id character varying(255),
    px double precision,
    py double precision,
    pz double precision,
    vx double precision,
    vy double precision,
    vz double precision,
    ts bigint
);


ALTER TABLE public.h_position OWNER TO postgres;

--
-- Name: u_info; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.u_info (
    uav_id bigint NOT NULL,
    a double precision,
    priority integer
);


ALTER TABLE public.u_info OWNER TO postgres;

--
-- Name: u_position; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.u_position (
    uav_id character varying(255),
    px double precision,
    py double precision,
    pz double precision,
    vx double precision,
    vy double precision,
    vz double precision,
    theta double precision,
    phi double precision,
    ts bigint
);


ALTER TABLE public.u_position OWNER TO postgres;

--
-- Name: u_shape; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.u_shape (
    uav_id character varying(255) NOT NULL,
    xu double precision,
    xl double precision,
    yu double precision,
    yl double precision,
    zu double precision,
    zl double precision
);


ALTER TABLE public.u_shape OWNER TO postgres;

--
-- Data for Name: h_position; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.h_position (human_id, px, py, pz, vx, vy, vz, ts) FROM stdin;
human/10020	100	100	1	1	1	1	100
human/10005	300	50	1	2	2	1	100
human/10012	30	50	1	2	2	1	100
human/10015	100	70	1	2	2	1	100
\.


--
-- Data for Name: u_info; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.u_info (uav_id, a, priority) FROM stdin;
vehicle/1000001	20	1
vehicle/1000002	20	2
vehicle/1000003	10	1
vehicle/1000004	10	2
vehicle/1000005	10	1
\.


--
-- Data for Name: u_position; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.u_position (uav_id, px, py, pz, vx, vy, vz, theta, phi, ts) FROM stdin;
vehicle/1000001	200	100	200	1	0	1	10	25	100
vehicle/1000002	250	250	120	1	1	0	20	30	100
vehicle/1000003	150	50	320	0	1	0	50	30	100
vehicle/1000004	130	350	420	0	1	1	70	10	100
vehicle/1000005	125	345	410	0	1	1	0	0	100
vehicle/1000001	255	255	122	10	10	0	20	30	110
vehicle/1000002	250	250	120	20	18	0	20	30	110
vehicle/1000003	150	50	320	0	100	0	50	30	110
vehicle/1000004	130	350	420	0	41	21	70	10	110
vehicle/1000005	125	345	410	0	15	100	0	0	110
vehicle/1000004	130	350	420	0	41	21	70	10	111
vehicle/1000005	129	349	419	0	15	100	0	0	111
\.


--
-- Data for Name: u_shape; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.u_shape (uav_id, xu, xl, yu, yl, zu, zl) FROM stdin;
vehicle/1000001	1	-1	1	-2	2	-1
vehicle/1000002	2	-1.5	1	-2	0.5	-0.5
vehicle/1000003	1	-1.5	1	-0.5	0.5	-0.5
vehicle/1000004	2	-1.5	1	-2	0.5	-0.5
vehicle/1000005	10	-11.5	12	-20	10.5	-10.5
\.


--
-- Name: u_info u_info_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.u_info
    ADD CONSTRAINT u_info_pkey PRIMARY KEY (uav_id);


--
-- Name: u_shape u_shape_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.u_shape
    ADD CONSTRAINT u_shape_pkey PRIMARY KEY (uav_id);


--
-- PostgreSQL database dump complete
--

