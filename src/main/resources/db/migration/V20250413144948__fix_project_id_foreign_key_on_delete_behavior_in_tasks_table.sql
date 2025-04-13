ALTER TABLE tasks
  DROP FOREIGN KEY (project_id),
  ADD FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE;