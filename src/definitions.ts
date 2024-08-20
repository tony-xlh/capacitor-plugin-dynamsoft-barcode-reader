export interface DBRPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
