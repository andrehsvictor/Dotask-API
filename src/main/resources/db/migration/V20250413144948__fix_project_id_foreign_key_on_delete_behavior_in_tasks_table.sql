ALTER TABLE tasks
  DROP CONSTRAINT tasks_project_id_fkey;
                    
ALTER TABLE tasks
  ADD FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE;