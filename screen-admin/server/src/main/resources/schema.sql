CREATE TABLE IF NOT EXISTS admin_user (
    id IDENTITY PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(128) NOT NULL,
    password_salt VARCHAR(32) NOT NULL,
    display_name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS work_item (
    id VARCHAR(64) PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    cover_url VARCHAR(1000),
    description VARCHAR(2000),
    tags VARCHAR(500),
    release_year INT NOT NULL,
    role VARCHAR(100),
    period VARCHAR(100),
    project_url VARCHAR(1000),
    tech_stack VARCHAR(500),
    content CLOB,
    result CLOB,
    images CLOB,
    sort_no INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE work_item ADD COLUMN IF NOT EXISTS role VARCHAR(100);
ALTER TABLE work_item ADD COLUMN IF NOT EXISTS period VARCHAR(100);
ALTER TABLE work_item ADD COLUMN IF NOT EXISTS project_url VARCHAR(1000);
ALTER TABLE work_item ADD COLUMN IF NOT EXISTS tech_stack VARCHAR(500);
ALTER TABLE work_item ADD COLUMN IF NOT EXISTS content CLOB;
ALTER TABLE work_item ADD COLUMN IF NOT EXISTS result CLOB;
ALTER TABLE work_item ADD COLUMN IF NOT EXISTS images CLOB;
ALTER TABLE work_item ADD COLUMN IF NOT EXISTS view_count BIGINT NOT NULL DEFAULT 0;
ALTER TABLE work_item ADD COLUMN IF NOT EXISTS visitor_count BIGINT NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS comic_item (
    id VARCHAR(64) PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    cover_url VARCHAR(1000),
    status VARCHAR(30) NOT NULL,
    chapters INT NOT NULL,
    description VARCHAR(2000),
    sort_no INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS comic_chapter (
    id VARCHAR(64) PRIMARY KEY,
    comic_id VARCHAR(64) NOT NULL,
    chapter_no INT NOT NULL,
    title VARCHAR(150) NOT NULL,
    summary VARCHAR(1000),
    pages CLOB,
    published_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comic_chapter_comic FOREIGN KEY (comic_id) REFERENCES comic_item(id) ON DELETE CASCADE
);

ALTER TABLE comic_chapter ADD COLUMN IF NOT EXISTS pages CLOB;

ALTER TABLE comic_item ADD COLUMN IF NOT EXISTS view_count BIGINT NOT NULL DEFAULT 0;
ALTER TABLE comic_item ADD COLUMN IF NOT EXISTS visitor_count BIGINT NOT NULL DEFAULT 0;

CREATE INDEX IF NOT EXISTS idx_comic_chapter_list ON comic_chapter(comic_id, chapter_no, title);

CREATE TABLE IF NOT EXISTS movie_item (
    id VARCHAR(64) PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    cover_url VARCHAR(1000),
    play_url VARCHAR(1000),
    release_year INT NOT NULL,
    rating DECIMAL(3, 1) NOT NULL,
    genre VARCHAR(50) NOT NULL,
    note VARCHAR(1000),
    watch_date DATE,
    director VARCHAR(150),
    cast_names VARCHAR(500),
    review CLOB,
    story_score INT,
    visual_score INT,
    sound_score INT,
    rewatch_value INT,
    sort_no INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE movie_item ADD COLUMN IF NOT EXISTS watch_date DATE;
ALTER TABLE movie_item ADD COLUMN IF NOT EXISTS play_url VARCHAR(1000);
ALTER TABLE movie_item ADD COLUMN IF NOT EXISTS director VARCHAR(150);
ALTER TABLE movie_item ADD COLUMN IF NOT EXISTS cast_names VARCHAR(500);
ALTER TABLE movie_item ADD COLUMN IF NOT EXISTS review CLOB;
ALTER TABLE movie_item ADD COLUMN IF NOT EXISTS story_score INT;
ALTER TABLE movie_item ADD COLUMN IF NOT EXISTS visual_score INT;
ALTER TABLE movie_item ADD COLUMN IF NOT EXISTS sound_score INT;
ALTER TABLE movie_item ADD COLUMN IF NOT EXISTS rewatch_value INT;
ALTER TABLE movie_item ADD COLUMN IF NOT EXISTS view_count BIGINT NOT NULL DEFAULT 0;
ALTER TABLE movie_item ADD COLUMN IF NOT EXISTS visitor_count BIGINT NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS anime_item (
    id VARCHAR(64) PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    cover_url VARCHAR(1000),
    play_url VARCHAR(1000),
    release_year INT NOT NULL,
    rating DECIMAL(3, 1) NOT NULL,
    genre VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    episodes INT NOT NULL,
    note VARCHAR(1000),
    review CLOB,
    sort_no INT NOT NULL DEFAULT 0,
    view_count BIGINT NOT NULL DEFAULT 0,
    visitor_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE anime_item ADD COLUMN IF NOT EXISTS view_count BIGINT NOT NULL DEFAULT 0;
ALTER TABLE anime_item ADD COLUMN IF NOT EXISTS play_url VARCHAR(1000);
ALTER TABLE anime_item ADD COLUMN IF NOT EXISTS visitor_count BIGINT NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS article_item (
    id VARCHAR(64) PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    description VARCHAR(2000),
    tags VARCHAR(500),
    publish_date DATE NOT NULL,
    content CLOB,
    status VARCHAR(20) NOT NULL DEFAULT 'published',
    sort_no INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE article_item ADD COLUMN IF NOT EXISTS content CLOB;
ALTER TABLE article_item ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'published';
ALTER TABLE article_item ADD COLUMN IF NOT EXISTS scheduled_at TIMESTAMP;
ALTER TABLE article_item ADD COLUMN IF NOT EXISTS view_count BIGINT NOT NULL DEFAULT 0;
ALTER TABLE article_item ADD COLUMN IF NOT EXISTS visitor_count BIGINT NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS content_visitor_log (
    content_type VARCHAR(20) NOT NULL,
    content_id VARCHAR(64) NOT NULL,
    visitor_key VARCHAR(64) NOT NULL,
    view_count BIGINT NOT NULL DEFAULT 0,
    first_viewed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_viewed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_content_visitor_log PRIMARY KEY (content_type, content_id, visitor_key)
);

CREATE INDEX IF NOT EXISTS idx_content_visitor_content
    ON content_visitor_log(content_type, content_id, last_viewed_at);

CREATE TABLE IF NOT EXISTS home_page_config (
    config_key VARCHAR(32) PRIMARY KEY,
    config_json CLOB NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS model_item (
    id VARCHAR(64) PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    category VARCHAR(50) NOT NULL,
    cover_url VARCHAR(1000),
    file_url VARCHAR(1000) NOT NULL,
    description VARCHAR(2000),
    tags VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'published',
    sort_no INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_model_sort ON model_item(status, sort_no, title);

CREATE TABLE IF NOT EXISTS model_progress_node (
    id VARCHAR(64) PRIMARY KEY,
    model_id VARCHAR(64) NOT NULL,
    parent_id VARCHAR(64),
    node_type VARCHAR(20) NOT NULL,
    name VARCHAR(150) NOT NULL,
    mesh_key VARCHAR(200),
    status VARCHAR(20) NOT NULL DEFAULT 'not_started',
    progress_percent INT NOT NULL DEFAULT 0,
    weight NUMERIC(10, 2) NOT NULL DEFAULT 1,
    planned_start_date DATE,
    planned_end_date DATE,
    actual_start_date DATE,
    actual_end_date DATE,
    owner VARCHAR(100),
    delay_reason VARCHAR(500),
    remark VARCHAR(1000),
    sort_no INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_model_progress_model
    ON model_progress_node(model_id, sort_no, node_type, name);

CREATE TABLE IF NOT EXISTS model_progress_snapshot (
    id VARCHAR(64) PRIMARY KEY,
    model_id VARCHAR(64) NOT NULL,
    nodes_json CLOB NOT NULL,
    captured_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_model_progress_snapshot_model
    ON model_progress_snapshot(model_id, captured_at DESC);

CREATE TABLE IF NOT EXISTS model_progress_audit (
    id VARCHAR(64) PRIMARY KEY,
    model_id VARCHAR(64) NOT NULL,
    action VARCHAR(50) NOT NULL,
    detail CLOB NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_model_progress_audit_model
    ON model_progress_audit(model_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_work_sort ON work_item(sort_no, release_year, title);
CREATE INDEX IF NOT EXISTS idx_comic_sort ON comic_item(sort_no, title);
CREATE INDEX IF NOT EXISTS idx_movie_sort ON movie_item(sort_no, release_year, title);
CREATE INDEX IF NOT EXISTS idx_anime_sort ON anime_item(sort_no, release_year, title);
CREATE INDEX IF NOT EXISTS idx_article_sort ON article_item(sort_no, publish_date, title);

CREATE TABLE IF NOT EXISTS contact_message (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    contact VARCHAR(100) NOT NULL,
    content VARCHAR(1000) NOT NULL,
    handled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_contact_created ON contact_message(created_at);

CREATE TABLE IF NOT EXISTS content_comment (
    id VARCHAR(64) PRIMARY KEY,
    content_type VARCHAR(20) NOT NULL,
    content_id VARCHAR(64) NOT NULL,
    visitor_key VARCHAR(64) NOT NULL,
    author_name VARCHAR(50) NOT NULL,
    body VARCHAR(1000) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'approved',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_comment_content
    ON content_comment(content_type, content_id, created_at);

CREATE INDEX IF NOT EXISTS idx_comment_status
    ON content_comment(status, created_at);

CREATE TABLE IF NOT EXISTS comic_reading_progress (
    visitor_key VARCHAR(64) NOT NULL,
    comic_id VARCHAR(64) NOT NULL,
    chapter_id VARCHAR(64) NOT NULL,
    chapter_no INT NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_reading_progress PRIMARY KEY (visitor_key, comic_id),
    CONSTRAINT fk_progress_comic FOREIGN KEY (comic_id) REFERENCES comic_item(id) ON DELETE CASCADE
);
