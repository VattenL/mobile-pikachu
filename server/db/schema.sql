-- Tạo lại các bảng tblPlayer, tblMatch, tblMatchResult theo Hình 3.2 (xóa bảng cũ theo thứ tự ngược với khóa ngoại).
-- Chạy trên CSDL đã tạo bằng create-db.sql:
--   mysql -u pikachu -p pikachu < server/db/schema.sql
-- Xem docs/pha4-cai-dat.md, mục 1d. Phụ trách: Dev 2.

DROP TABLE IF EXISTS tblMatchResult;
DROP TABLE IF EXISTS tblMatch;
DROP TABLE IF EXISTS tblPlayer;

CREATE TABLE tblPlayer (
  id        INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
  username  VARCHAR(50)  NOT NULL UNIQUE,
  password  VARCHAR(64)  NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE tblMatch (
  id         INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
  startTime  DATETIME     NOT NULL,
  endTime    DATETIME     NOT NULL,
  timeLimit  INT          NOT NULL,
  tiles      VARCHAR(300) NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE tblMatchResult (
  id          INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
  result      VARCHAR(10)  NOT NULL,
  finishTime  INT          NULL,
  tiles       VARCHAR(300) NOT NULL,
  matchId     INT          NOT NULL,
  playerId    INT          NOT NULL,
  UNIQUE (matchId, playerId),
  FOREIGN KEY (matchId)  REFERENCES tblMatch(id) ON DELETE CASCADE,
  FOREIGN KEY (playerId) REFERENCES tblPlayer(id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
